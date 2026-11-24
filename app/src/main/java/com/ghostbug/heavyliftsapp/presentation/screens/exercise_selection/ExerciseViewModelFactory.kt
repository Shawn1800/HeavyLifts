package com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository

class ExerciseViewModelFactory(
    private val exerciseRepository: ExerciseRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return ExerciseViewModel(exerciseRepository) as T
    }
}
