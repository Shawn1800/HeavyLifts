package com.ghostbug.heavyliftsapp.presentation.screens.home

import kotlinx.datetime.LocalDate


sealed interface HomeEvent {
    data class OnDateSelected(val date: LocalDate) : HomeEvent
    data class EditWorkout(val exerciseId: Long) : HomeEvent
    object RefreshWorkouts : HomeEvent

    data class Message(val message: String): HomeEvent
}