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
import com.ghostbug.heavyliftsapp.presentation.screens.progress.ProgressLoadState
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class LogWorkoutScreenTest {
    @Test
    fun retriesMultipleSetsAndAppendsToTheReopenedLift() = runBlocking {
        withContext(Dispatchers.Main) {
            val exercise = ExerciseEntity(id = 1L, exerciseName = "Squat", category = "Legs")
            val workoutRepository = InMemoryWorkoutRepository()
            val viewModel = createViewModel(exercise, workoutRepository)
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
                val unsavedProgress = awaitProgress(viewModel)
                assertTrue(unsavedProgress.chartPoints.isEmpty())

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
                val savedProgress = awaitProgress(viewModel)
                assertEquals(80f, savedProgress.chartPoints.single().weight, 0f)
                viewModel.onEvent(addSet)
                viewModel.onEvent(LogWorkoutEvent.SaveWorkout())
                yield()
                assertEquals(listOf(1, 2, 3), workoutRepository.savedSets.map { it.sets })
            } finally {
                collector.cancel()
            }
        }
    }

    @Test
    fun savesDecimalWeightsAndKeepsStagedSetsWhenInputIsInvalid() = runBlocking {
        withContext(Dispatchers.Main) {
            val exercise = ExerciseEntity(id = 1L, exerciseName = "Squat", category = "Legs")
            val workoutRepository = InMemoryWorkoutRepository().apply { rejectInsert = false }
            val viewModel = createViewModel(exercise, workoutRepository)
            val events = Channel<LogWorkoutUiEvent>(Channel.UNLIMITED)
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                viewModel.uiEvent.collect { events.send(it) }
            }

            try {
                viewModel.onEvent(LogWorkoutEvent.SetExerciseById(exercise.id, System.currentTimeMillis()))
                viewModel.onEvent(LogWorkoutEvent.OnAddingSets(exercise.id, "80,5", "6"))
                val stagedSets = viewModel.state.value.sets
                assertEquals(80.5f, stagedSets.single().weight, 0f)
                assertEquals(6, stagedSets.single().reps)

                val invalidInputs = listOf(
                    "" to "",
                    "82.25" to "",
                    "-82.25" to "5",
                    "82.25" to "0",
                    "Infinity" to "5",
                    "82.25" to "5.5"
                )
                for ((weight, reps) in invalidInputs) {
                    viewModel.onEvent(LogWorkoutEvent.OnAddingSets(exercise.id, weight, reps))
                    assertTrue(withTimeout(5.seconds) { events.receive() } is LogWorkoutUiEvent.SendSnackbar)
                    assertEquals(stagedSets, viewModel.state.value.sets)

                    viewModel.onEvent(LogWorkoutEvent.SaveWorkout(weight, reps))
                    assertTrue(withTimeout(5.seconds) { events.receive() } is LogWorkoutUiEvent.SendSnackbar)
                    assertEquals(stagedSets, viewModel.state.value.sets)
                    assertTrue(workoutRepository.savedSets.isEmpty())
                }

                viewModel.onEvent(LogWorkoutEvent.SaveWorkout("82.25", "5"))
                assertEquals(LogWorkoutUiEvent.NavBackToHome, withTimeout(5.seconds) { events.receive() })
                assertEquals(listOf(80.5f, 82.25f), workoutRepository.savedSets.map { it.weight })
                assertEquals(listOf(6, 5), workoutRepository.savedSets.map { it.reps })
            } finally {
                collector.cancel()
            }
        }
    }

    private suspend fun awaitProgress(viewModel: LogWorkoutViewModel): ProgressLoadState.Ready =
        withTimeout(5.seconds) {
            viewModel.state.map { it.progress }.filterIsInstance<ProgressLoadState.Ready>().first()
        }

    private fun createViewModel(
        exercise: ExerciseEntity,
        workoutRepository: WorkoutRepository,
    ): LogWorkoutViewModel {
        val oneRepMaxRepository = StubOneRepMaxRepository()
        return LogWorkoutViewModel(
            workoutRepository = workoutRepository,
            oneRepMaxRepository = oneRepMaxRepository,
            oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository),
            exerciseRepository = StubExerciseRepository(exercise)
        )
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
