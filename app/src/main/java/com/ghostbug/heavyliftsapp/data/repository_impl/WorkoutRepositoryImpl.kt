package com.ghostbug.heavyliftsapp.data.repository_impl

import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutWithExercise
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.data.remote.dto.WorkoutEntryEntityDto
import com.ghostbug.heavyliftsapp.data.remote.dto.WorkoutWithExerciseDto
import com.ghostbug.heavyliftsapp.data.remote.dto.toDomain
import com.ghostbug.heavyliftsapp.data.remote.dto.toDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class WorkoutRepositoryImpl(
    private val postgrest: Postgrest,
    private val auth: Auth
) : WorkoutRepository {

    private companion object {
        const val TABLE_WORKOUT_ENTRIES = "workout_entries"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_DATE = "date"
        const val COLUMN_EXERCISE_ID = "exercise_id"
        const val COLUMN_WEIGHT = "weight"
        const val COLUMN_SET_NUMBER = "sets"
        const val COLUMN_ID = "id"
        const val EXERCISES_JOIN = "*, exercises(*)"
        const val HISTORY_PAGE_SIZE = 1_000
    }

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun insertWorkoutEntry(entries: List<WorkoutEntryEntity>) {
        val userId = checkNotNull(currentUserId) { "Sign in to save your workout" }
        withContext(Dispatchers.IO) {
            val dtos = entries.map { it.toDto().copy(userId = userId) }
            postgrest.from(TABLE_WORKOUT_ENTRIES).insert(dtos)
        }
    }

    override suspend fun getWorkoutByDate(date: Long): List<WorkoutWithExercise> {
        val userId = currentUserId ?: return emptyList()

        return withContext(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDateRange(date)

            postgrest.from(TABLE_WORKOUT_ENTRIES)
                .select(Columns.raw(EXERCISES_JOIN)) {
                    filter {
                        eq(COLUMN_USER_ID, userId)
                        and {
                            gte(COLUMN_DATE, startOfDay)
                            lt(COLUMN_DATE, endOfDay)
                        }
                    }
                }
                .decodeList<WorkoutWithExerciseDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun deleteSetById(id: Long) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            postgrest.from(TABLE_WORKOUT_ENTRIES)
                .delete {
                    filter {
                        eq(COLUMN_USER_ID, userId)
                        eq(COLUMN_ID, id)
                    }
                }
        }
    }

    override suspend fun getWorkoutByExerciseAndDate(
        exerciseId: Long,
        date: Long
    ): List<WorkoutEntryEntity> {
        val userId = currentUserId ?: return emptyList()

        return withContext(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDateRange(date)

            postgrest.from(TABLE_WORKOUT_ENTRIES)
                .select {
                    filter {
                        eq(COLUMN_USER_ID, userId)
                        eq(COLUMN_EXERCISE_ID, exerciseId)
                        and {
                            gte(COLUMN_DATE, startOfDay)
                            lt(COLUMN_DATE, endOfDay)
                        }
                    }
                    order(COLUMN_SET_NUMBER, Order.ASCENDING)
                    order(COLUMN_ID, Order.ASCENDING)
                }
                .decodeList<WorkoutEntryEntityDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun getWorkoutsByExercise(exerciseId: Long): List<WorkoutEntryEntity> {
        val userId = currentUserId ?: return emptyList()
        return withContext(Dispatchers.IO) {
            val entries = buildList {
                var lastId: Long? = null
                var total: Long? = null
                do {
                    val afterId = lastId
                    val result = postgrest.from(TABLE_WORKOUT_ENTRIES)
                        .select {
                            if (afterId == null) count(Count.EXACT)
                            filter {
                                eq(COLUMN_USER_ID, userId)
                                eq(COLUMN_EXERCISE_ID, exerciseId)
                                if (afterId != null) gt(COLUMN_ID, afterId)
                            }
                            order(COLUMN_ID, Order.ASCENDING)
                            limit(HISTORY_PAGE_SIZE.toLong())
                        }
                    if (afterId == null) total = result.countOrNull()
                    val page = result.decodeList<WorkoutEntryEntityDto>()
                    page.forEach { add(it.toDomain()) }
                    lastId = page.lastOrNull()?.id
                    val hasMore = total?.let { size < it } ?: (page.size == HISTORY_PAGE_SIZE)
                } while (lastId != null && hasMore)
            }
            entries.sortedBy { it.date }
        }
    }

    override suspend fun getMaxWeightByExercise(exerciseId: Long): Float? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            postgrest.from(TABLE_WORKOUT_ENTRIES)
                .select(Columns.list(COLUMN_WEIGHT)) {
                    filter {
                        eq(COLUMN_USER_ID, userId)
                        eq(COLUMN_EXERCISE_ID, exerciseId)
                    }
                    order(COLUMN_WEIGHT, Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<Map<String, Float>>()
                ?.get(COLUMN_WEIGHT)
        }
    }

    private fun getDateRange(date: Long): Pair<String, String> {
        val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
        val endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
        return Pair(startOfDay, endOfDay)
    }
}
