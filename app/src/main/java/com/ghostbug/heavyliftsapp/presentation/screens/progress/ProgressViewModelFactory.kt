package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository

class ProgressViewModelFactory(
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return ProgressViewModel(
            oneRepMaxRepository,
            workoutRepository,
            exerciseRepository
        ) as T
    }
}
