package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

sealed interface LogWorkoutUiEvent {
    data class SendSnackbar(val message:String) : LogWorkoutUiEvent
    object  NavBackToHome: LogWorkoutUiEvent
}