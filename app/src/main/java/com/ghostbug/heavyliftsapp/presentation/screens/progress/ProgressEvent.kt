package com.ghostbug.heavyliftsapp.presentation.screens.progress

sealed interface ProgressEvent {
    data class currPr(val exerciseId:Long): ProgressEvent
    data class bestWeight(val exerciseId:Long): ProgressEvent
}