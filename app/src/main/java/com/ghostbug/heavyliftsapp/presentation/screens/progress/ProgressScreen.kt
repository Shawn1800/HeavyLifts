package com.ghostbug.heavyliftsapp.presentation.screens.progress

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.domain.model.ProgressMetric
import com.ghostbug.heavyliftsapp.domain.model.ProgressRange
import com.ghostbug.heavyliftsapp.domain.model.ProgressSession
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetBadgeShape
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProgressScreen(
    progressViewModel: ProgressViewModel,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val state by progressViewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        progressViewModel.loadProgress()
    }

    ProgressContent(
        state = state,
        onExerciseSelected = progressViewModel::selectExercise,
        onMetricSelected = progressViewModel::selectMetric,
        onRangeSelected = progressViewModel::selectRange,
        onRetry = progressViewModel::loadProgress,
        modifier = modifier,
        bottomBarPadding = bottomBarPadding,
    )
}

@Composable
private fun ProgressContent(
    state: ProgressState,
    onExerciseSelected: (Long) -> Unit,
    onMetricSelected: (ProgressMetric) -> Unit,
    onRangeSelected: (ProgressRange) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    var showExercisePicker by rememberSaveable { mutableStateOf(false) }

    if (showExercisePicker && state.exercises.isNotEmpty()) {
        ExercisePickerSheet(
            exercises = state.exercises,
            selectedExerciseId = state.selectedExercise?.id,
            onDismiss = { showExercisePicker = false },
            onSelect = { id ->
                onExerciseSelected(id)
                showExercisePicker = false
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = HeavyLiftsColors.Bg,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets(bottom = bottomBarPadding)),
    ) { insets ->
        val layoutDirection = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = insets.calculateStartPadding(layoutDirection),
                    top = insets.calculateTopPadding(),
                    end = insets.calculateEndPadding(layoutDirection),
                )
                .consumeWindowInsets(insets),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 24.dp,
                end = 16.dp,
                bottom = insets.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExerciseHeader(
                    exercise = state.selectedExercise,
                    canSelectExercise = state.exercises.isNotEmpty(),
                    onSelectExercise = { showExercisePicker = true }
                )
            }
            when (val loadState = state.loadState) {
                ProgressLoadState.Loading -> item { ProgressLoading() }
                ProgressLoadState.Error -> item {
                    ProgressMessage(
                        title = stringResource(R.string.progress_load_failed),
                        detail = stringResource(R.string.progress_load_failed_detail),
                        onRetry = onRetry
                    )
                }
                ProgressLoadState.NoExercises -> item {
                    ProgressMessage(
                        title = stringResource(R.string.progress_no_exercises),
                        detail = stringResource(R.string.progress_no_exercises_detail),
                        onRetry = onRetry
                    )
                }
                is ProgressLoadState.Ready -> {
                    val history = loadState.history
                    val heaviestSet = history.heaviestSet
                    if (heaviestSet == null) {
                        item {
                            ProgressMessage(
                                title = stringResource(R.string.progress_no_lifts),
                                detail = stringResource(R.string.progress_no_lifts_detail)
                            )
                        }
                    } else {
                        item {
                            PersonalBestCard(
                                weight = heaviestSet.weight,
                                estimatedMax = history.bestEstimatedMax,
                                changePercent = history.allTimeWeightChangePercent
                            )
                        }
                        item {
                            ProgressionCard(loadState, state.metric, state.range, onMetricSelected, onRangeSelected)
                        }
                        item { ProgressPersonalRecords(history) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseHeader(
    exercise: ExerciseEntity?,
    canSelectExercise: Boolean,
    onSelectExercise: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (exercise != null) {
                Text(
                    text = stringResource(R.string.progress_heading_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = HeavyLiftsColors.AccentStrong,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp
                )
            }
            Text(
                text = exercise?.exerciseName ?: stringResource(R.string.progress_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.25).sp
            )
            if (exercise != null) {
                Text(exercise.category, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (canSelectExercise) {
            FilledTonalIconButton(
                onClick = onSelectExercise,
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = HeavyLiftsColors.BgElevated,
                    contentColor = HeavyLiftsColors.NavyStrong
                )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.progress_select_exercise)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    exercises: List<ExerciseEntity>,
    selectedExerciseId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = HeavyLiftsColors.Bg) {
        Text(
            text = stringResource(R.string.progress_select_exercise),
            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                val selected = exercise.id == selectedExerciseId
                ListItem(
                    headlineContent = { Text(exercise.exerciseName) },
                    supportingContent = { Text(exercise.category) },
                    trailingContent = if (selected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = HeavyLiftsColors.AccentStrong) }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .selectable(selected, role = Role.RadioButton, onClick = { onSelect(exercise.id) })
                )
            }
        }
    }
}

@Composable
private fun PersonalBestCard(
    weight: Float,
    estimatedMax: Float?,
    changePercent: Float?
) {
    val locale = LocalConfiguration.current.locales[0]
    Surface(
        color = HeavyLiftsColors.Accent,
        contentColor = HeavyLiftsColors.NavyStrong,
        shape = RoundedCornerShape(40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.progress_all_time_pr),
                        style = MaterialTheme.typography.labelMedium,
                        color = HeavyLiftsColors.NavyStrong,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = stringResource(R.string.progress_weight_kg, formatProgressNumber(weight, locale)),
                        style = MaterialTheme.typography.displayLarge,
                        color = HeavyLiftsColors.NavyStrong,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                        autoSize = TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = 64.sp),
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier.size(72.dp).background(HeavyLiftsColors.AccentStrong, SetBadgeShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = HeavyLiftsColors.White
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (changePercent != null) {
                    val percent = NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 1 }
                    SummaryPill(
                        stringResource(R.string.progress_weight_change, percent.format(changePercent / 100.0))
                    )
                }
                if (estimatedMax != null) {
                    SummaryPill(
                        stringResource(R.string.progress_estimated_pr, formatProgressNumber(estimatedMax, locale))
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(text: String) {
    Surface(color = HeavyLiftsColors.White, shape = CircleShape) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = HeavyLiftsColors.Navy
        )
    }
}

@Composable
internal fun ExerciseProgressSection(
    progress: ProgressLoadState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var metric by rememberSaveable { mutableStateOf(ProgressMetric.TopWeight) }
    var range by rememberSaveable { mutableStateOf(ProgressRange.Month) }

    Box(modifier.fillMaxWidth()) {
        when (progress) {
            ProgressLoadState.Loading -> ProgressLoading()
            ProgressLoadState.Error -> ProgressMessage(
                title = stringResource(R.string.progress_load_failed),
                detail = stringResource(R.string.progress_load_failed_detail),
                onRetry = onRetry,
            )
            ProgressLoadState.NoExercises -> Unit
            is ProgressLoadState.Ready -> {
                if (progress.history.heaviestSet == null) {
                    ProgressMessage(
                        title = stringResource(R.string.progress_no_lifts),
                        detail = stringResource(R.string.progress_no_lifts_detail),
                    )
                } else {
                    val chartState = remember(progress.history, metric, range) {
                        progress.history.chartState(metric, range)
                    }
                    ProgressionCard(chartState, metric, range, { metric = it }, { range = it })
                }
            }
        }
    }
}

@Composable
private fun ProgressionCard(
    progress: ProgressLoadState.Ready,
    metric: ProgressMetric,
    range: ProgressRange,
    onMetricSelected: (ProgressMetric) -> Unit,
    onRangeSelected: (ProgressRange) -> Unit
) {
    Surface(shape = RoundedCornerShape(24.dp), color = HeavyLiftsColors.BgOverlay) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.progress_progression),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            ProgressSelector(
                options = ProgressMetric.entries,
                selected = metric,
                label = { stringResource(it.label) },
                onSelect = onMetricSelected
            )
            ProgressSelector(
                options = ProgressRange.entries,
                selected = range,
                label = { stringResource(it.label) },
                onSelect = onRangeSelected
            )
            if (progress.chartPoints.isEmpty()) {
                Text(
                    text = stringResource(R.string.progress_no_sessions_in_range),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                progress.latestSession?.let { session ->
                    LatestSessionStrip(session)
                }
                ProgressTrendChart(progress.chartPoints, stringResource(metric.accessibleLabel))
            }
        }
    }
}

@Composable
private fun <T> ProgressSelector(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .selectable(isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
                    .heightIn(min = 48.dp)
                    .widthIn(min = 48.dp),
                shape = CircleShape,
                color = if (isSelected) HeavyLiftsColors.AccentStrong else HeavyLiftsColors.White
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) HeavyLiftsColors.White else HeavyLiftsColors.NavyStrong,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LatestSessionStrip(session: ProgressSession) {
    val locale = LocalConfiguration.current.locales[0]
    val date = session.date.format(
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "yMMMd"), locale)
    )
    val weight = formatProgressNumber(session.topSet.weight, locale)
    val reps = pluralStringResource(R.plurals.progress_record_reps, session.topSet.reps, session.topSet.reps)

    Surface(color = HeavyLiftsColors.White, shape = MaterialTheme.shapes.small) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                date,
                style = MaterialTheme.typography.labelMedium,
                color = HeavyLiftsColors.AccentStrong,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.progress_session_set, weight, reps),
                style = MaterialTheme.typography.labelMedium,
                color = HeavyLiftsColors.Navy
            )
            session.weightRecordIncrease?.let { increase ->
                Text(
                    text = stringResource(R.string.progress_session_pr_increase, formatProgressNumber(increase, locale)),
                    modifier = Modifier
                        .background(HeavyLiftsColors.AccentSoft, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = HeavyLiftsColors.NavyStrong,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProgressLoading() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = HeavyLiftsColors.AccentStrong,
            strokeWidth = 3.dp
        )
        Text(stringResource(R.string.progress_loading), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProgressMessage(title: String, detail: String, onRetry: (() -> Unit)? = null) {
    Surface(color = HeavyLiftsColors.BgOverlay, shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(HeavyLiftsColors.AccentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(detail, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            if (onRetry != null) {
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.progress_try_again), color = HeavyLiftsColors.NavyStrong)
                }
            }
        }
    }
}

private fun formatProgressNumber(value: Float, locale: Locale): String =
    NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }.format(value)

@get:StringRes
private val ProgressMetric.label: Int
    get() = when (this) {
        ProgressMetric.TopWeight -> R.string.progress_metric_top_weight
        ProgressMetric.EstimatedOneRepMax -> R.string.progress_metric_estimated_max
    }

@get:StringRes
private val ProgressMetric.accessibleLabel: Int
    get() = when (this) {
        ProgressMetric.TopWeight -> R.string.progress_metric_top_weight
        ProgressMetric.EstimatedOneRepMax -> R.string.progress_metric_estimated_max_full
    }

@get:StringRes
private val ProgressRange.label: Int
    get() = when (this) {
        ProgressRange.Week -> R.string.progress_range_week
        ProgressRange.Month -> R.string.progress_range_month
        ProgressRange.Year -> R.string.progress_range_year
        ProgressRange.All -> R.string.progress_range_all
    }
