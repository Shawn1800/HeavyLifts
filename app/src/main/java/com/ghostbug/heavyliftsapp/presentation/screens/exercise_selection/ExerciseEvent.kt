package com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection

import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity

sealed class ExerciseEvent {
    data class OnSearchQueryChange(val query :String ): ExerciseEvent()
    data class OnSelectCategory(val category: String?): ExerciseEvent()
    data object OnClearCategory : ExerciseEvent()
    data class OnAddExercise(val exercise: ExerciseEntity): ExerciseEvent()

}
