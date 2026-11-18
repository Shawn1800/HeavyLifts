package com.ghostbug.heavyliftsapp.domain.repository

import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutWithExercise

interface WorkoutRepository {
    suspend fun insertWorkoutEntry(entries: List<WorkoutEntryEntity>)
    
    suspend fun getWorkoutByDate(date: Long): List<WorkoutWithExercise>
    
    suspend fun deleteSetById(id: Long)
    
    suspend fun getWorkoutByExerciseAndDate(exerciseId: Long, date: Long): List<WorkoutEntryEntity>

    suspend fun getWorkoutsByExercise(exerciseId: Long): List<WorkoutEntryEntity>
}
