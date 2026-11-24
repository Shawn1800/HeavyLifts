package com.ghostbug.heavyliftsapp.presentation.screens.progress

import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.domain.model.ProgressChartPoint
import com.ghostbug.heavyliftsapp.domain.model.ProgressHistory
import com.ghostbug.heavyliftsapp.domain.model.ProgressMetric
import com.ghostbug.heavyliftsapp.domain.model.ProgressRange
import com.ghostbug.heavyliftsapp.domain.model.ProgressSession

data class ProgressState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val selectedExercise: ExerciseEntity? = null,
    val metric: ProgressMetric = ProgressMetric.TopWeight,
    val range: ProgressRange = ProgressRange.Month,
    val loadState: ProgressLoadState = ProgressLoadState.Loading,
)

sealed interface ProgressLoadState {
    data object Loading : ProgressLoadState
    data object Error : ProgressLoadState
    data object NoExercises : ProgressLoadState

    data class Ready(
        val history: ProgressHistory,
        val chartPoints: List<ProgressChartPoint>,
        val latestSession: ProgressSession?,
    ) : ProgressLoadState
}
