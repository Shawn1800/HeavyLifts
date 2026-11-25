package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.domain.usecase.GetExerciseProgressUseCase
import com.ghostbug.heavyliftsapp.domain.usecase.OneRepMaxUseCase

class LogWorkoutViewModelFactory(
    private val repository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase,
    private val exerciseRepository: ExerciseRepository,
    private val getExerciseProgress: GetExerciseProgressUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(LogWorkoutViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return LogWorkoutViewModel(
            repository,
            oneRepMaxRepository,
            oneRepMaxUseCase,
            exerciseRepository,
            getExerciseProgress,
        ) as T
    }
}

