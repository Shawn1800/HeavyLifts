package com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection

import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity


data class ExerciseState (
    val exercises : List<ExerciseEntity> = emptyList(),
    val searchQuery :String="",
    val selectedCategory : String?=null,
    val isSearching : Boolean=false,
)