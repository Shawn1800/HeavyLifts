package com.ghostbug.heavyliftsapp.presentation.screens.home

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetBadgeShape
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetNumberBadge
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

// ─── Helper Functions ─────────────────────────────────────────────────────────

private fun getExerciseInitials(name: String): String {
    val words = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        words.size >= 2 -> "${words[0].first().uppercaseChar()}${words[1].first().lowercaseChar()}"
        words.size == 1 -> name.take(2).replaceFirstChar { it.uppercase() }
        else -> "Ex"
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToLogWorkout: (Long) -> Unit,
    bottomBarPadding: Dp = 0.dp,
) {
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        homeViewModel.onEvent(HomeEvent.RefreshWorkouts)
        homeViewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToLogWorkout -> onNavigateToLogWorkout(event.exerciseId)
                is HomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets(bottom = bottomBarPadding)),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        HomeContent(
            state = state,
            paddingValues = paddingValues,
            onDateSelected = { date -> homeViewModel.onEvent(HomeEvent.OnDateSelected(date)) },
            onEvent = homeViewModel::onEvent,
        )
    }
}

// ─── Content Layout ───────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    state: HomeState,
    paddingValues: PaddingValues,
    onDateSelected: (LocalDate) -> Unit,
    onEvent: (HomeEvent) -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val weekCount = 500
    val firstMonday = today.minus(today.dayOfWeek.ordinal + (weekCount - 1) * 7, DateTimeUnit.DAY)
    val selectedWeek = (firstMonday.daysUntil(state.selectedDate) / 7).coerceIn(0, weekCount - 1)
    val pagerState = rememberPagerState(initialPage = selectedWeek) { weekCount }
    val canEdit = state.isTodaySelected
    val context = LocalContext.current
    LaunchedEffect(selectedWeek) {
        pagerState.scrollToPage(selectedWeek)
    }

    val layoutDirection = LocalLayoutDirection.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(layoutDirection),
            )
            .consumeWindowInsets(paddingValues)
            .background(HeavyLiftsColors.Bg)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 12.dp),
        ) {
            item {
                DateHeaderSection(state.selectedDate) {
                    val date = state.selectedDate
                    DatePickerDialog(context, { _, year, month, day ->
                        onDateSelected(LocalDate(year, month + 1, day))
                    }, date.year, date.month.ordinal, date.day).apply {
                        val zone = TimeZone.currentSystemDefault()
                        datePicker.minDate = firstMonday.atStartOfDayIn(zone).toEpochMilliseconds()
                        datePicker.maxDate = today.atStartOfDayIn(zone).toEpochMilliseconds()
                    }.show()
                }
                Spacer(Modifier.height(16.dp))
                WeeklyCalendar(pagerState, firstMonday, state.selectedDate, onDateSelected)
                if (!state.isLoading && state.errorMessage == null && state.workouts.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    SessionSummaryCard(state.workouts, Modifier.padding(horizontal = 16.dp))
                }
                Text(
                    text = "Today's exercises",
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 10.dp),
                    color = HeavyLiftsColors.Fg1,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            when {
                state.isLoading -> item { HeavyLoadingIndicator() }
                state.errorMessage != null -> item {
                    HeavyErrorMessage(state.errorMessage) { onEvent(HomeEvent.RefreshWorkouts) }
                }
                state.workouts.isEmpty() -> item { HeavyEmptyState(canEdit) }
                else -> items(state.workouts, key = { "${state.selectedDate}:${it.exercise.id}" }) { workout ->
                    HeavyWorkoutCard(
                        workout = workout,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        onEdit = if (canEdit) {
                            { onEvent(HomeEvent.EditWorkout(workout.exercise.id)) }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun DateHeaderSection(
    selectedDate: LocalDate,
    onCalendarClick: () -> Unit
) {
    val dayOfWeekUpper = remember(selectedDate) {
        selectedDate.dayOfWeek.name
    }

    val formattedDateStr = remember(selectedDate) {
        val monthShort = selectedDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        "${selectedDate.day} $monthShort"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 20.dp, top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = dayOfWeekUpper,
                color = HeavyLiftsColors.AccentStrong,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedDateStr,
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 56.sp,
                letterSpacing = (-2).sp,
                lineHeight = 60.sp
            )
        }

        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier
                .size(52.dp)
                .padding(bottom = 12.dp)
                .background(HeavyLiftsColors.BgElevated,
                    RoundedCornerShape(18.dp))
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select Date",
                tint = HeavyLiftsColors.NavyStrong,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SessionSummaryCard(workouts: List<GroupedWorkout>, modifier: Modifier = Modifier) {
    val setCount = workouts.sumOf { it.sets.size }
    val repCount = workouts.sumOf { workout -> workout.sets.sumOf { it.reps } }
    val records = workouts.filter { it.changePercent?.let { change -> change.isFinite() && change > 0f } == true }
    val exerciseCount = workouts.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.Accent, RoundedCornerShape(40.dp))
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_session_summary),
                    color = HeavyLiftsColors.NavyStrong,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = pluralStringResource(R.plurals.home_sets_logged, setCount, setCount),
                    color = HeavyLiftsColors.NavyStrong,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
            }
            Box(
                modifier = Modifier.size(56.dp).background(HeavyLiftsColors.AccentStrong, SetBadgeShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp).rotate(-45f)
                )
            }
        }
        Text(
            text = stringResource(
                R.string.home_session_counts,
                pluralStringResource(R.plurals.home_exercise_count, exerciseCount, exerciseCount),
                pluralStringResource(R.plurals.home_rep_count, repCount, repCount)
            ),
            color = HeavyLiftsColors.NavyStrong,
            fontFamily = HeavyLiftsType.Body,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        if (records.isNotEmpty()) {
            Surface(color = Color.White, shape = CircleShape) {
                Text(
                    text = if (records.size == 1) {
                        stringResource(R.string.home_exercise_pr, records.single().exercise.exerciseName)
                    } else {
                        stringResource(R.string.home_multiple_exercise_prs, records.size)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    color = HeavyLiftsColors.NavyStrong,
                    fontFamily = HeavyLiftsType.Body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ─── Weekly Calendar ──────────────────────────────────────────────────────────

@Composable
private fun WeeklyCalendar(
    pagerState: PagerState,
    firstMonday: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) { page ->
        WeekRow(
            startDate = firstMonday.plus(page * 7, DateTimeUnit.DAY),
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun WeekRow(
    startDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(HeavyLiftsColors.BgOverlay, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0..6) {
            val date = startDate.plus(i, DateTimeUnit.DAY)
            HeavyDateItem(
                date = date,
                modifier = Modifier.weight(if (date == selectedDate) 1.45f else 1f),
                isSelected = date == selectedDate,
                isFuture = date > today,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun HeavyDateItem(
    date: LocalDate,
    modifier: Modifier,
    isSelected: Boolean,
    isFuture: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayLetter = date.dayOfWeek.name.take(1)
    val dayNumber = date.day.toString()

    val pillColor by animateColorAsState(
        targetValue = when {
            isSelected -> HeavyLiftsColors.AccentStrong
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "datePillColor"
    )

    val textColor = when {
        isSelected -> Color.White
        isFuture -> HeavyLiftsColors.Fg3
        else -> HeavyLiftsColors.Fg1
    }

    Box(
        modifier = modifier
            .height(60.dp)
            .clip(CircleShape)
            .background(pillColor)
            .clickable(
                enabled = !isFuture,
                role = Role.Button
            ) { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayLetter,
                color = if (isSelected) Color.White else HeavyLiftsColors.Fg3,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dayNumber,
                color = textColor,
                fontFamily = HeavyLiftsType.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}

// ─── Workout Card ─────────────────────────────────────────────────────────────

@Composable
private fun HeavyWorkoutCard(
    workout: GroupedWorkout,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val toggleSets = { expanded = !expanded }
    val setsAction = stringResource(if (expanded) R.string.home_hide_sets else R.string.home_view_sets)
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(200),
        label = "workoutCardChevron"
    )

    val initials = remember(workout.exercise.exerciseName) {
        getExerciseInitials(workout.exercise.exerciseName)
    }

    val topSet = workout.sets.maxByOrNull { it.weight } ?: workout.sets.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HeavyLiftsColors.BgOverlay)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (onEdit == null) setsAction else null,
                    onClick = onEdit ?: toggleSets
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Initials Icon Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HeavyLiftsColors.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = HeavyLiftsColors.NavyStrong,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Exercise Title & Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.exercise.exerciseName,
                    color = HeavyLiftsColors.Fg1,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = buildString {
                        append(workout.exercise.category)
                        if (topSet != null) {
                            append(" · ${topSet.weight} kg × ${topSet.reps}")
                        }
                    },
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Percentage badge
            workout.changePercent?.let { percent ->
                val label = (if (percent >= 0) "+" else "") + "%.0f%%".format(percent)
                Surface(
                    color = HeavyLiftsColors.AccentStrong,
                    shape = CircleShape
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontFamily = HeavyLiftsType.Body,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (onEdit == null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = HeavyLiftsColors.Fg3,
                    modifier = Modifier.size(24.dp).rotate(chevronRotation)
                )
            } else {
                IconButton(onClick = toggleSets) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = setsAction,
                        tint = HeavyLiftsColors.Fg3,
                        modifier = Modifier.rotate(chevronRotation)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(220)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                workout.sets.forEachIndexed { index, set ->
                    HeavySetRow(
                        setNumber = index + 1,
                        weight = set.weight,
                        reps = set.reps,
                        onClick = onEdit
                    )
                }
            }
        }
    }
}

@Composable
private fun HeavySetRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetNumberBadge(
            number = setNumber,
            modifier = Modifier.size(36.dp),
            containerColor = HeavyLiftsColors.AccentStrong
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "SET $setNumber",
            color = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "%.1f".format(weight),
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "kg",
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "× $reps",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Empty / Loading / Error States ──────────────────────────────────────────

@Composable
private fun HeavyLoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = HeavyLiftsColors.AccentStrong,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Loading session...",
                color = HeavyLiftsColors.Fg3,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun HeavyErrorMessage(message: String, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(HeavyLiftsColors.Danger, CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Something went wrong",
                color = HeavyLiftsColors.Danger,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = HeavyLiftsColors.Fg3,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Try again", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun HeavyEmptyState(canEdit: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(HeavyLiftsColors.AccentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No sets logged yet",
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (canEdit) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Tap + to add your first exercise",
                    color = HeavyLiftsColors.Fg3,
                    fontSize = 14.sp
                )
            }
        }
    }
}
