package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.domain.usecase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class LogWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LogWorkoutState())
    val state: StateFlow<LogWorkoutState> = _state.asStateFlow()
    private val _uiEvent = MutableSharedFlow<LogWorkoutUiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<LogWorkoutUiEvent> = _uiEvent.asSharedFlow()
    private var loadJob: Job? = null

    fun onEvent(event: LogWorkoutEvent) {
        when (event) {
            is LogWorkoutEvent.OnAddingSets -> handleAddSet(event.exerciseId, event.weight, event.reps)
            is LogWorkoutEvent.DeleteSet -> handleDeleteSet(event.id)
            is LogWorkoutEvent.UpdateReps -> handleUpdateReps(event.id, event.reps)
            is LogWorkoutEvent.UpdateWeight -> handleUpdateWeight(event.id, event.weight)
            is LogWorkoutEvent.SetExerciseById -> handleSetExerciseById(event)
            is LogWorkoutEvent.SaveWorkout -> saveWorkout(event.currentWeight, event.currentReps)
        }
    }

    private fun handleSetExerciseById(event: LogWorkoutEvent.SetExerciseById) {
        loadJob?.cancel()
        _state.update { it.copy(exercise = null, sets = emptyList(), dateMillis = event.dateMillis) }
        loadJob = viewModelScope.launch {
            try {
                val exercise = exerciseRepository.getExerciseById(event.exerciseId)
                if (exercise != null) {
                    val list = workoutRepository.getWorkoutByExerciseAndDate(event.exerciseId, event.dateMillis)
                    _state.update {
                        it.copy(exercise = exercise, dateMillis = event.dateMillis, sets = list)
                    }
                } else {
                    sendSnackbar("Exercise not found")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                sendSnackbar("Error loading exercise: ${e.message}")
            }
        }
    }

    private fun handleAddSet(exerciseId: Long, weightStr: String, repsStr: String) {
        if (_state.value.isSaving || _state.value.exercise?.id != exerciseId) return
        val weight = weightStr.toDoubleOrNull()
        val reps = repsStr.toIntOrNull()

        if (weight == null || reps == null || weight == 0.0 || reps == 0) {
            sendSnackbar("Enter valid weight and reps")
            return
        }

        _state.update { currentState ->
            val setNumber = currentState.nextSetNumber
            val newSet = WorkoutEntryEntity(
                id = -setNumber.toLong(),
                exerciseId = exerciseId,
                weight = weight.toFloat(),
                reps = reps,
                sets = setNumber,
                date = currentState.dateMillis,
                userId = ""
            )
            currentState.copy(sets = currentState.sets + newSet)
        }
    }

    private fun handleDeleteSet(id: Long) {
        if (_state.value.isSaving) return
        if (id < 0) {
            _state.update { state -> state.copy(sets = state.sets.filter { it.id != id }) }
            return
        }

        viewModelScope.launch {
            try {
                if (id > 0) workoutRepository.deleteSetById(id)
                
                val updatedSets = _state.value.sets.filter { it.id != id }
                _state.update { it.copy(sets = updatedSets) }

                val exerciseId = _state.value.exercise?.id ?: return@launch
                val normalizedDate = getNormalizedDate(_state.value.dateMillis)

                if (updatedSets.isEmpty()) {
                    oneRepMaxRepository.deleteOneRepMax(exerciseId, normalizedDate)
                } else {
                    oneRepMaxUseCase(exerciseId, normalizedDate, updatedSets)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                sendSnackbar("Error deleting: ${e.message}")
            }
        }
    }

    private fun handleUpdateReps(id: Long, reps: String) {
        // no-op: logged set text fields use local Compose state; not synced to VM
    }

    private fun handleUpdateWeight(id: Long, weight: String) {
        // no-op: logged set text fields use local Compose state; not synced to VM
    }


    private fun saveWorkout(currentWeightStr: String = "", currentRepsStr: String = "") {
        val currentState = _state.value
        val exerciseId = currentState.exercise?.id ?: return
        if (currentState.isSaving) return

        val dateToSave = getNormalizedDate(currentState.dateMillis)
        val curWeight = currentWeightStr.toDoubleOrNull()
        val curReps = currentRepsStr.toIntOrNull()

        val newSets = currentState.sets
            .filter { it.id <= 0L }
            .map { it.copy(id = 0L, date = dateToSave) }
            .toMutableList()
        if (curWeight != null && curReps != null && curWeight > 0 && curReps > 0) {
            newSets.add(
                WorkoutEntryEntity(
                    exerciseId = exerciseId,
                    weight = curWeight.toFloat(),
                    reps = curReps,
                    sets = currentState.nextSetNumber,
                    date = dateToSave,
                    userId = ""
                )
            )
        }

        if (newSets.isEmpty() && currentState.sets.isEmpty()) {
            sendSnackbar("Add at least 1 set")
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (newSets.isNotEmpty()) {
                    workoutRepository.insertWorkoutEntry(newSets)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendSnackbar("Error saving: ${e.message}")
                return@launch
            } finally {
                _state.update { it.copy(isSaving = false) }
            }

            _uiEvent.emit(LogWorkoutUiEvent.NavBackToHome)

            if (newSets.isNotEmpty()) {
                try {
                    val allSets = currentState.sets.filter { it.id > 0L } + newSets
                    oneRepMaxUseCase(exerciseId, dateToSave, allSets)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // The sets are saved even if personal records cannot be refreshed.
                }
            }
        }
    }

    private fun getNormalizedDate(millis: Long): Long {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch { _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar(message)) }
    }
}
