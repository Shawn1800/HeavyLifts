package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.domain.model.ProgressHistory
import com.ghostbug.heavyliftsapp.domain.model.ProgressMetric
import com.ghostbug.heavyliftsapp.domain.model.ProgressRange
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.usecase.GetExerciseProgressUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val getExerciseProgress: GetExerciseProgressUseCase,
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProgressState(
            metric = ProgressMetric.entries.firstOrNull { it.name == savedStateHandle.get<String>(KEY_METRIC) }
                ?: ProgressMetric.TopWeight,
            range = ProgressRange.entries.firstOrNull { it.name == savedStateHandle.get<String>(KEY_RANGE) }
                ?: ProgressRange.Month,
        )
    )
    val uiState: StateFlow<ProgressState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadProgress() {
        if (loadJob?.isActive == true) return
        requestProgress(uiState.value.selectedExercise)
    }

    fun selectExercise(exerciseId: Long) {
        val state = uiState.value
        if (state.selectedExercise?.id == exerciseId) return
        state.exercises.firstOrNull { it.id == exerciseId }?.let(::requestProgress)
    }

    fun selectMetric(metric: ProgressMetric) {
        savedStateHandle[KEY_METRIC] = metric.name
        _uiState.update { it.copy(metric = metric).withUpdatedChart() }
    }

    fun selectRange(range: ProgressRange) {
        savedStateHandle[KEY_RANGE] = range.name
        _uiState.update { it.copy(range = range).withUpdatedChart() }
    }

    private fun requestProgress(exercise: ExerciseEntity?) {
        loadJob?.cancel()
        _uiState.update { it.copy(selectedExercise = exercise, loadState = ProgressLoadState.Loading) }
        loadJob = viewModelScope.launch {
            try {
                val selected = exercise ?: loadExerciseCatalog()
                if (selected == null) {
                    _uiState.update { it.copy(loadState = ProgressLoadState.NoExercises) }
                    return@launch
                }
                savedStateHandle[KEY_EXERCISE] = selected.id
                val history = getExerciseProgress(selected.id)
                ensureActive()
                _uiState.update { it.withHistory(history) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                ensureActive()
                _uiState.update { it.copy(loadState = ProgressLoadState.Error) }
            }
        }
    }

    private suspend fun loadExerciseCatalog(): ExerciseEntity? {
        val exercises = exerciseRepository.getAllExercises().sortedBy { it.exerciseName.lowercase() }
        val savedId = savedStateHandle.get<Long>(KEY_EXERCISE)
        val selected = exercises.firstOrNull { it.id == savedId }
            ?: exercises.firstOrNull { it.exerciseName.equals("Bench Press", ignoreCase = true) }
            ?: exercises.firstOrNull()
        currentCoroutineContext().ensureActive()
        _uiState.update { it.copy(exercises = exercises, selectedExercise = selected) }
        return selected
    }

    private fun ProgressState.withUpdatedChart(): ProgressState {
        val ready = loadState as? ProgressLoadState.Ready ?: return this
        return withHistory(ready.history)
    }

    private fun ProgressState.withHistory(history: ProgressHistory): ProgressState = copy(
        loadState = history.chartState(metric, range),
    )

    private companion object {
        const val KEY_EXERCISE = "exercise_id"
        const val KEY_METRIC = "progress_metric_name"
        const val KEY_RANGE = "progress_range_name"
    }
}
