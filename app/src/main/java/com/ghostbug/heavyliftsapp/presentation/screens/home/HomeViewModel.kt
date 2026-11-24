package com.ghostbug.heavyliftsapp.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.presentation.screens.home.HomeUiEvent.NavigateToExerciseSelection
import com.ghostbug.heavyliftsapp.presentation.screens.home.HomeUiEvent.NavigateToLogWorkout
import com.ghostbug.heavyliftsapp.presentation.screens.home.HomeUiEvent.ShowSnackbar
import com.ghostbug.heavyliftsapp.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlin.time.Clock


data class GroupedWorkout(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutEntryEntity>,
    val changePercent: Float? = null
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var workoutJob: Job? = null

    init {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        onEvent(HomeEvent.OnDateSelected(today))

        viewModelScope.launch {
            oneRepMaxRepository.updates.collect {
                _state.value.selectedDateMillis?.let { observeWorkoutsForDate(it) }
            }
        }

    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnDateSelected -> {
                val millis = event.date
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

                _state.update {
                    it.copy(
                        selectedDate = event.date,
                        selectedDateMillis = millis,
                        isLoading = true,
                        errorMessage = null
                    )
                }
                observeWorkoutsForDate(millis)
            }

            is HomeEvent.OnAddWorkoutClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateToExerciseSelection(
                            dateMillis = _state.value.selectedDateMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            is HomeEvent.EditWorkout -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateToLogWorkout(
                            exerciseId = event.exerciseId,
                            dateMillis = _state.value.selectedDateMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            is HomeEvent.RefreshWorkouts -> refresh()

            is HomeEvent.Message -> {
                viewModelScope.launch { _uiEvent.emit(ShowSnackbar(event.message)) }
            }
        }
    }

    fun refresh() {
        _state.value.selectedDateMillis?.let { observeWorkoutsForDate(it) }
    }

    private fun observeWorkoutsForDate(dateMillis: Long) {
        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                supervisorScope {
                    val workoutsDeferred = async { workoutRepository.getWorkoutByDate(dateMillis) }
                    val maxesDeferred = async { oneRepMaxRepository.getOneRepMaxForDate(dateMillis) }

                    val workouts = workoutsDeferred.await()
                    val oneRepMaxes = try {
                        maxesDeferred.await()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        emptyList()
                    }

                    val oneRMMap = oneRepMaxes.associateBy { it.exerciseId }

                    val grouped = workouts
                        .groupBy { it.exercise.id }
                        .map { (exerciseId, entries) ->
                            GroupedWorkout(
                                exercise = entries.first().exercise,
                                sets = entries.map { w ->
                                    WorkoutEntryEntity(
                                        id = w.id,
                                        exerciseId = w.exercise.id,
                                        weight = w.weight.toFloat(),
                                        reps = w.reps,
                                        sets = w.sets,
                                        date = w.date,
                                        userId = w.userId
                                    )
                                },
                                changePercent = oneRMMap[exerciseId]?.changePercent?.toFloat()
                            )
                        }

                    ensureActive()
                    _state.update { it.copy(workouts = grouped, isLoading = false) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(isLoading = false, errorMessage = "No internet connection or server error") }
            }
        }
    }
}
