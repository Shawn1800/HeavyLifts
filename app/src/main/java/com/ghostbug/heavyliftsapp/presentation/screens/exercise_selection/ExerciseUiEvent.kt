package com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection

import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity

sealed interface ExerciseUiEvent {
    data class  AddExercise(val exercise: ExerciseEntity): ExerciseUiEvent  //this is used to go  to the logworkoutscreen
    object NavigateBack: ExerciseUiEvent
}