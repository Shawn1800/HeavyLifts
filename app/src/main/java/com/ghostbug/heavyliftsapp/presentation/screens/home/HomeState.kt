package com.ghostbug.heavyliftsapp.presentation.screens.home

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class HomeState(
    val selectedDate: LocalDate =Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val selectedDateMillis: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val workouts: List<GroupedWorkout> = emptyList()
)
