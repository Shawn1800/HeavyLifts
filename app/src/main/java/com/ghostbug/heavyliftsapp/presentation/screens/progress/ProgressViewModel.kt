package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProgressUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var bestWeightJob: Job? = null

    suspend fun loadExercises() {
        _state.update { it.copy(loadingExercises = true, exerciseLoadFailed = false) }
        try {
            val exercises = exerciseRepository.getAllExercises()
                .sortedBy { it.exerciseName.lowercase() }
            _state.update { it.copy(exercises = exercises, loadingExercises = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(loadingExercises = false, exerciseLoadFailed = true) }
        }
    }

    fun onEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.currPr -> {
                viewModelScope.launch {
                    oneRepMaxRepository.getLatest(event.exerciseId)
                }
            }

            is ProgressEvent.bestWeight -> {
                loadBestWeight(event.exerciseId)
            }
        }
    }

    private fun loadBestWeight(exerciseId: Long) {
        bestWeightJob?.cancel()
        _state.update { it.copy(weightExerciseId = exerciseId, maxWeight = null, weightLoadFailed = false) }
        bestWeightJob = viewModelScope.launch {
            try {
                val weight = workoutRepository.getMaxWeightByExercise(exerciseId)
                ensureActive()
                _state.update { it.copy(maxWeight = weight) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                _state.update { it.copy(weightLoadFailed = true) }
            }
        }
    }
}
