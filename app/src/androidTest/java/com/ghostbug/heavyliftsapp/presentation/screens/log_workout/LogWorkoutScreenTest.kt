package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.domain.model.OneRepMaxEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutWithExercise
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.domain.usecase.OneRepMaxUseCase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogWorkoutScreenTest {
    @Test
    fun retriesMultipleSetsAndAppendsToTheReopenedLift() = runBlocking {
        withContext(Dispatchers.Main) {
            val exercise = ExerciseEntity(id = 1L, exerciseName = "Squat", category = "Legs")
            val workoutRepository = InMemoryWorkoutRepository()
            val oneRepMaxRepository = StubOneRepMaxRepository()
            val viewModel = LogWorkoutViewModel(
                workoutRepository = workoutRepository,
                oneRepMaxRepository = oneRepMaxRepository,
                oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository),
                exerciseRepository = StubExerciseRepository(exercise)
            )
            val openLift = LogWorkoutEvent.SetExerciseById(exercise.id, System.currentTimeMillis())
            val addSet = LogWorkoutEvent.OnAddingSets(exercise.id, "80", "5")
            val events = mutableListOf<LogWorkoutUiEvent>()
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                viewModel.uiEvent.collect { events.add(it) }
            }

            try {
                viewModel.onEvent(openLift)
                viewModel.onEvent(addSet)
                viewModel.onEvent(addSet)
                assertEquals(listOf(1, 2), viewModel.state.value.sets.map { it.sets })

                viewModel.onEvent(LogWorkoutEvent.SaveWorkout())
                yield()
                assertEquals(LogWorkoutUiEvent.SendSnackbar("Error saving: Insert failed"), events.single())
                assertEquals(2, viewModel.state.value.sets.size)
                assertTrue(workoutRepository.savedSets.isEmpty())

                workoutRepository.rejectInsert = false
                viewModel.onEvent(LogWorkoutEvent.SaveWorkout())
                yield()
                assertEquals(LogWorkoutUiEvent.NavBackToHome, events.last())
                assertEquals(listOf(1, 2), workoutRepository.savedSets.map { it.sets })

                viewModel.onEvent(openLift)
                assertTrue(viewModel.state.value.sets.all { it.id > 0L })
                viewModel.onEvent(addSet)
                viewModel.onEvent(LogWorkoutEvent.SaveWorkout())
                yield()
                assertEquals(listOf(1, 2, 3), workoutRepository.savedSets.map { it.sets })
            } finally {
                collector.cancel()
            }
        }
    }

    private class InMemoryWorkoutRepository : WorkoutRepository {
        val savedSets = mutableListOf<WorkoutEntryEntity>()
        var rejectInsert = true

        override suspend fun insertWorkoutEntry(entries: List<WorkoutEntryEntity>) {
            check(!rejectInsert) { "Insert failed" }
            for (entry in entries) {
                savedSets.add(entry.copy(id = savedSets.size + 1L))
            }
        }

        override suspend fun getWorkoutByExerciseAndDate(exerciseId: Long, date: Long) =
            savedSets.toList()

        override suspend fun getWorkoutsByExercise(exerciseId: Long) = savedSets.toList()

        override suspend fun getWorkoutByDate(date: Long): List<WorkoutWithExercise> = emptyList()
        override suspend fun deleteSetById(id: Long) = Unit
        override suspend fun getMaxWeightByExercise(exerciseId: Long): Float? = null
    }

    private class StubExerciseRepository(private val exercise: ExerciseEntity) : ExerciseRepository {
        override suspend fun getExerciseById(id: Long) = exercise.takeIf { it.id == id }
        override suspend fun insertExercises(exercises: List<ExerciseEntity>) = Unit
        override suspend fun getAllExercises() = listOf(exercise)
        override suspend fun searchExercises(query: String): List<ExerciseEntity> = emptyList()
        override suspend fun getExerciseByCategory(category: String): List<ExerciseEntity> = emptyList()
        override suspend fun searchExerciseByCategory(query: String, category: String): List<ExerciseEntity> =
            emptyList()
    }

    private class StubOneRepMaxRepository : OneRepMaxRepository {
        override val updates = MutableSharedFlow<Unit>()

        override suspend fun insert(entity: OneRepMaxEntity) = Unit
        override suspend fun getChangePercentageForDate(exerciseId: Long, date: Long): Double? = null
        override suspend fun getOneRepMaxForDate(date: Long): List<OneRepMaxEntity> = emptyList()
        override suspend fun getLatest(exerciseId: Long): OneRepMaxEntity? = null
        override suspend fun getPersonalBestBefore(exerciseId: Long, date: Long): OneRepMaxEntity? = null
        override suspend fun getNext(exerciseId: Long, date: Long): OneRepMaxEntity? = null
        override suspend fun deleteOneRepMax(exerciseId: Long, date: Long) = Unit
        override suspend fun deleteByExercise(exerciseId: Long) = Unit
        override suspend fun update(entity: OneRepMaxEntity) = Unit
        override suspend fun getByDate(exerciseId: Long, date: Long): OneRepMaxEntity? = null
    }
}
