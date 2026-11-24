package com.ghostbug.heavyliftsapp.domain.usecase

import com.ghostbug.heavyliftsapp.domain.model.OneRepMaxEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.domain.model.estimatedOneRepMax
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository

class OneRepMaxUseCase(
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(
        exerciseId: Long,
        date: Long,
        sets: List<WorkoutEntryEntity>
    ) {

        recalculateTimeline(exerciseId)
    }

    private suspend fun recalculateTimeline(exerciseId: Long) {

        val allWorkouts = workoutRepository.getWorkoutsByExercise(exerciseId)
        if (allWorkouts.isEmpty()) {
            oneRepMaxRepository.deleteByExercise(exerciseId)
            return
        }
        val sessionsByDate = allWorkouts.groupBy { 
            val instant = java.time.Instant.ofEpochMilli(it.date)
            instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        }.toSortedMap()


        oneRepMaxRepository.deleteByExercise(exerciseId)

        var runningPersonalBest = 0.0f
        
        sessionsByDate.forEach { (localDate, sessionSets) ->
            val sessionDateMillis = localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val sessionMax1RM = sessionSets.maxOf { it.estimatedOneRepMax() }
            val prevBest = runningPersonalBest
            val changePercent = if (prevBest > 0) ((sessionMax1RM - prevBest) / prevBest) * 100 else 0.0f

            oneRepMaxRepository.insert(
                OneRepMaxEntity(
                    id = 0,
                    exerciseId = exerciseId,
                    curr1RM = sessionMax1RM,
                    prev1RM = prevBest,
                    changePercent = changePercent,
                    date = sessionDateMillis,
                    userId = ""
                )
            )

            if (sessionMax1RM > runningPersonalBest) {
                runningPersonalBest = sessionMax1RM
            }
        }
    }
}
