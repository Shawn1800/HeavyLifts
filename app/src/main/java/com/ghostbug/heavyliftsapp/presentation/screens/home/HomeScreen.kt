package com.ghostbug.heavyliftsapp.presentation.screens.home

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyliftsapp.presentation.screens.signup.SignUpEvent
import com.ghostbug.heavyliftsapp.presentation.screens.signup.SignUpUiEvent
import com.ghostbug.heavyliftsapp.presentation.screens.signup.SignUpViewModel
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetNumberBadge
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    signUpViewModel: SignUpViewModel = viewModel(),
    onNavigateToExerciseSelection: () -> Unit,
    onNavigateToLogWorkout: (Long) -> Unit,
    onNavigateToLogIn: () -> Unit
) {
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        homeViewModel.onEvent(HomeEvent.RefreshWorkouts)
        launch {
            signUpViewModel.uiEvent.collect { event ->
                if (event is SignUpUiEvent.NavigateToSignIn) onNavigateToLogIn()
            }
        }
        launch {
            homeViewModel.uiEvent.collect { event ->
                when (event) {
                    is HomeUiEvent.NavigateToExerciseSelection -> onNavigateToExerciseSelection()
                    is HomeUiEvent.NavigateToLogWorkout -> onNavigateToLogWorkout(event.exerciseId)
                    is HomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (showLogoutDialog) {
        HeavyLogoutDialog(
            onDismissRequest = { showLogoutDialog = false },
            onLogoutClick = {
                showLogoutDialog = false
                signUpViewModel.onEvent(SignUpEvent.OnNavigateToSignIn)
            }
        )
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        HomeContent(
            state = state,
            paddingValues = paddingValues,
            onDateSelected = { date -> homeViewModel.onEvent(HomeEvent.OnDateSelected(date)) },
            onEvent = { homeViewModel.onEvent(it) },
        )
    }
}

// ─── Content Layout ───────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    state: HomeState,
    paddingValues: PaddingValues,
    onDateSelected: (LocalDate) -> Unit,
    onEvent: (HomeEvent) -> Unit
) {
    val totalWeeks = 500
    val pagerState = rememberPagerState(
        pageCount = { totalWeeks },
        initialPage = totalWeeks - 1
    )

    val currentMonthName = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        Clock.System.todayIn(TimeZone.currentSystemDefault())
            .minus(weeksAgo * 7, DateTimeUnit.DAY).month.name
            .lowercase().replaceFirstChar { it.uppercase() }
    }

    val currentYear = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        Clock.System.todayIn(TimeZone.currentSystemDefault())
            .minus(weeksAgo * 7, DateTimeUnit.DAY).year.toString()
    }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(HeavyLiftsColors.Bg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeavyHeader(monthName = currentMonthName, year = currentYear)

        Spacer(modifier = Modifier.height(8.dp))

        WeeklyCalendar(
            pagerState = pagerState,
            selectedDate = state.selectedDate,
            onDateSelected = onDateSelected
        )

        Spacer(modifier = Modifier.height(30.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sessions",
                        color = HeavyLiftsColors.Fg1,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (today == state.selectedDate) {
                            "${state.workouts.size} today"
                        } else {
                            "${state.workouts.size} sessions"
                        },
                        color = HeavyLiftsColors.Fg2,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        state.isLoading -> HeavyLoadingIndicator()
                        state.errorMessage != null -> HeavyErrorMessage(
                            message = state.errorMessage,
                            onRetry = { onEvent(HomeEvent.RefreshWorkouts) }
                        )
                        state.workouts.isEmpty() -> HeavyEmptyState()
                        else -> WorkoutList(
                            workouts = state.workouts,
                            onEvent = onEvent,
                            state = state
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                HeavyFab(
                    onClick = {
                        if (today == state.selectedDate) {
                            onEvent(HomeEvent.OnAddWorkoutClick)
                        } else {
                            onEvent(HomeEvent.Message("Previous dates are read only"))
                        }
                    },
                    state = state
                )
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HeavyHeader(monthName: String, year: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = year,
            color = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = monthName,
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 58.sp,
            letterSpacing = (-1).sp,
            lineHeight = 64.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Logout Dialog ────────────────────────────────────────────────────────────

@Composable
fun HeavyLogoutDialog(onDismissRequest: () -> Unit, onLogoutClick: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Sign out",
                color = HeavyLiftsColors.Fg1,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You're about to sign out. Your data will remain intact.",
                color = HeavyLiftsColors.Fg3,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeavyLiftsColors.Fg2),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HeavyLiftsColors.Accent,
                        contentColor = HeavyLiftsColors.Fg1
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Sign out", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Weekly Calendar ──────────────────────────────────────────────────────────

@Composable
private fun WeeklyCalendar(
    pagerState: PagerState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val totalWeeks = 500
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val weeksAgo = (totalWeeks - 1) - page
        val dateInWeek = Clock.System.todayIn(TimeZone.currentSystemDefault())
            .minus(weeksAgo * 7, DateTimeUnit.DAY)
        val mondayOfWeek = dateInWeek.minus(dateInWeek.dayOfWeek.ordinal, DateTimeUnit.DAY)

        WeekRow(
            startDate = mondayOfWeek,
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
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0..6) {
            val date = startDate.plus(i, DateTimeUnit.DAY)
            val isSelected = date == selectedDate
            HeavyDateItem(
                modifier = Modifier.weight(if (isSelected) 1.7f else 1f),
                date = date,
                isToday = date == today,
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
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = date.dayOfWeek.name.take(1)
    val dayNumber = date.day.toString()

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> HeavyLiftsColors.Accent
            isFuture -> HeavyLiftsColors.BgOverlay
            else -> HeavyLiftsColors.BgElevated
        },
        animationSpec = tween(150),
        label = "dateColor"
    )

    Box(
        modifier = modifier
            .height(if (isSelected) 104.dp else 88.dp)
            .clip(RoundedCornerShape(if (isSelected) 32.dp else 24.dp))
            .clickable(
                enabled = !isFuture,
                role = androidx.compose.ui.semantics.Role.Button
            ) { onDateSelected(date) }
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                text = dayName,
                color = if (isSelected) HeavyLiftsColors.NavyStrong else HeavyLiftsColors.Fg2,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = dayNumber,
                color = if (isSelected || isToday) HeavyLiftsColors.NavyStrong else HeavyLiftsColors.Fg2,
                fontFamily = HeavyLiftsType.Display,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSelected) 26.sp else 19.sp
            )
        }
    }
}

// ─── FAB ─────────────────────────────────────────────────────────────────────

@Composable
private fun HeavyFab(onClick: () -> Unit, state: HomeState) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isToday = today == state.selectedDate
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
        },
        text = {
            Text(
                text = "Log session",
                fontFamily = HeavyLiftsType.Body,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = if (isToday) HeavyLiftsColors.Accent else HeavyLiftsColors.BgChip,
        contentColor = HeavyLiftsColors.NavyStrong,
        shape = RoundedCornerShape(28.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    )
}

// ─── Workout List ─────────────────────────────────────────────────────────────

@Composable
fun WorkoutList(
    workouts: List<GroupedWorkout>,
    onEvent: (HomeEvent) -> Unit,
    state: HomeState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(workouts.size) {
        if (workouts.isNotEmpty()) listState.animateScrollToItem(workouts.lastIndex)
    }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp)
    ) {
        items(
            items = workouts,
            key = { it.exercise.id }
        ) { workout ->
            HeavyWorkoutCard(
                workout = workout,
                onAddSetClick = if (today == state.selectedDate) {
                    { onEvent(HomeEvent.EditWorkout(workout.exercise.id)) }
                } else {
                    { onEvent(HomeEvent.Message("Previous dates are read only")) }
                }
            )
        }
    }
}

// ─── Workout Card ─────────────────────────────────────────────────────────────

@Composable
private fun HeavyWorkoutCard(
    workout: GroupedWorkout,
    onAddSetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "workoutCardChevron"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(HeavyLiftsColors.BgElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(HeavyLiftsColors.AccentMuted, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.exercise.exerciseName,
                    color = HeavyLiftsColors.Fg1,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = HeavyLiftsColors.BgChip,
                        shape = CircleShape
                    ) {
                        Text(
                            text = workout.exercise.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            color = HeavyLiftsColors.NavyStrong,
                            fontFamily = HeavyLiftsType.Body,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                    workout.changePercent?.let { percent ->
                        val label = (if (percent >= 0) "+" else "") + "%.0f%%".format(percent)
                        Surface(color = HeavyLiftsColors.AccentStrong, shape = CircleShape) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                color = HeavyLiftsColors.White,
                                fontFamily = HeavyLiftsType.Body,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(HeavyLiftsColors.AccentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse workout" else "Expand workout",
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(chevronRotation)
                )
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                workout.sets.forEachIndexed { index, set ->
                    HeavySetRow(
                        setNumber = index + 1,
                        weight = set.weight,
                        reps = set.reps,
                        onClick = onAddSetClick
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HeavyLiftsColors.BgOverlay)
            .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetNumberBadge(
            number = setNumber,
            modifier = Modifier.size(44.dp),
            containerColor = HeavyLiftsColors.AccentStrong
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "SET",
            color = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "%.1f".format(weight),
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "kg",
            color = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = "× ${reps}",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold
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
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Loading...",
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
private fun HeavyEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(HeavyLiftsColors.AccentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = HeavyLiftsColors.AccentStrong,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No sessions yet",
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Ready for today's session?",
                color = HeavyLiftsColors.Fg3,
                fontSize = 14.sp
            )
        }
    }
}
