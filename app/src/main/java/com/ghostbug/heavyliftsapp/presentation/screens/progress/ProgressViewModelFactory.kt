package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.usecase.GetExerciseProgressUseCase

internal fun progressViewModelFactory(
    getExerciseProgress: GetExerciseProgressUseCase,
    exerciseRepository: ExerciseRepository,
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ProgressViewModel(getExerciseProgress, exerciseRepository, createSavedStateHandle())
    }
}
