package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

sealed interface LogWorkoutEvent {
    // weight/reps carried in the event — no per-keystroke VM updates needed
    data class OnAddingSets(val exerciseId: Long, val weight: String, val reps: String) : LogWorkoutEvent
    data class SaveWorkout(val currentWeight: String? = null, val currentReps: String? = null) : LogWorkoutEvent
    data class DeleteSet(val id: Long) : LogWorkoutEvent
    data class SetExerciseById(val exerciseId: Long, val dateMillis: Long) : LogWorkoutEvent
    data object RetryProgress : LogWorkoutEvent
    // kept for potential future use (inline set editing)
    data class UpdateWeight(val id: Long, val weight: String) : LogWorkoutEvent
    data class UpdateReps(val id: Long, val reps: String) : LogWorkoutEvent
}