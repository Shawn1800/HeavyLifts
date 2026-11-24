package com.ghostbug.heavyliftsapp.domain.model


data class WorkoutEntryEntity(
    val id: Long=0L,
    val exerciseId: Long,
    val weight: Float,
    val reps: Int,
    val sets: Int,
    val date: Long,
    val userId: String
)

internal fun WorkoutEntryEntity.estimatedOneRepMax(): Float =
    if (reps > 1) weight * (1 + reps / 30f) else weight
