package com.ghostbug.heavyliftsapp.domain.usecase

import com.ghostbug.heavyliftsapp.domain.model.ProgressHistory
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetExerciseProgressUseCase(
    private val workoutRepository: WorkoutRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend operator fun invoke(exerciseId: Long): ProgressHistory {
        val entries = workoutRepository.getWorkoutsByExercise(exerciseId)
        return withContext(defaultDispatcher) { ProgressHistory(entries) }
    }
}
