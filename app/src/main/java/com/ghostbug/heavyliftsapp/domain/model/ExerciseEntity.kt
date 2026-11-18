package com.ghostbug.heavyliftsapp.domain.model

import kotlinx.serialization.SerialName

data class ExerciseEntity(
    val id: Long,
    @SerialName("exercise_name")
    val exerciseName: String,
    val category: String,

)