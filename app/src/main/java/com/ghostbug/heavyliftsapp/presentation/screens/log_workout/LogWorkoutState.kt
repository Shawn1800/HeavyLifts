package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.presentation.screens.progress.ProgressLoadState

data class LogWorkoutState(
    val selectedExerciseId: Long? = null,
    val sets: List<WorkoutEntryEntity> = emptyList(),
    val exercise: ExerciseEntity? = null,
    val dateMillis: Long = 0L,
    val isReadOnly: Boolean = false,
    val isSaving: Boolean = false,
    val progress: ProgressLoadState = ProgressLoadState.Loading,
) {
    val nextSetNumber: Int
        get() = (sets.maxOfOrNull { it.sets } ?: 0) + 1
}
