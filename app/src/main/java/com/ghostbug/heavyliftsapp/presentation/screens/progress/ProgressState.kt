package com.ghostbug.heavyliftsapp.presentation.screens.progress

import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity

data class ProgressState(
    val maxWeight: Float? = null,
    val weightExerciseId: Long? = null,
    val weightLoadFailed: Boolean = false,
    val exercises: List<ExerciseEntity> = emptyList(),
    val loadingExercises: Boolean = true,
    val exerciseLoadFailed: Boolean = false
)
