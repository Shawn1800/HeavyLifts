package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetNumberBadge
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch

@Composable
fun LogWorkoutScreen(
    exerciseId: Long,
    dateMillis: Long,
    logWorkoutViewModel: LogWorkoutViewModel = viewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(exerciseId, dateMillis) {
        logWorkoutViewModel.onEvent(
            LogWorkoutEvent.SetExerciseById(exerciseId, dateMillis)
        )
    }

    val state by logWorkoutViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        logWorkoutViewModel.uiEvent.collect { event ->
            when (event) {
                is LogWorkoutUiEvent.NavBackToHome -> onBack()
                is LogWorkoutUiEvent.SendSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        }
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LogWorkoutContent(
            state = state,
            onBack = onBack,
            onEvent = logWorkoutViewModel::onEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun LogWorkoutContent(
    state: LogWorkoutState,
    onBack: () -> Unit,
    onEvent: (LogWorkoutEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var weightValue by remember { mutableDoubleStateOf(1.0) }
    var repsValue by remember { mutableIntStateOf(20) }
    var inputChanged by remember { mutableStateOf(false) }

    LaunchedEffect(state.exercise?.id, state.dateMillis) {
        val lastSet = state.sets.lastOrNull()
        weightValue = lastSet?.weight?.toDouble() ?: 1.0
        repsValue = lastSet?.reps ?: 20
        inputChanged = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeavyLiftsColors.Bg)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 112.dp)
        ) {
            // ── Top Bar ─────────────────────────────────────────────
            LogTopBar(
                exerciseName = state.exercise?.exerciseName ?: "Exercise",
                onClose = onBack
            )

            // ── Set Sub-Heading ─────────────────────────────────────
            SectionHeading(text = "Set ${state.nextSetNumber}")

            Spacer(modifier = Modifier.height(10.dp))

            // ── Stepper Cards (Weight & Reps Side-by-Side) ──────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weight Stepper Card
                StepperCard(
                    modifier = Modifier.weight(1f),
                    label = "WEIGHT",
                    valueStr = "%.1f".format(weightValue),
                    unitStr = "kg",
                    onDecrement = {
                        if (weightValue > 0.5) {
                            weightValue -= 0.5
                            inputChanged = true
                        }
                    },
                    onIncrement = {
                        weightValue += 0.5
                        inputChanged = true
                    }
                )

                // Reps Stepper Card
                StepperCard(
                    modifier = Modifier.weight(1f),
                    label = "REPS",
                    valueStr = repsValue.toString(),
                    unitStr = "reps",
                    onDecrement = {
                        if (repsValue > 1) {
                            repsValue -= 1
                            inputChanged = true
                        }
                    },
                    onIncrement = {
                        repsValue += 1
                        inputChanged = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── "Lock in set" Primary Button ────────────────────────
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        val weightStr = weightValue.toString()
                        val repsStr = repsValue.toString()
                        onEvent(
                            LogWorkoutEvent.OnAddingSets(
                                exerciseId = state.exercise?.id ?: 0,
                                weight = weightStr,
                                reps = repsStr
                            )
                        )
                        inputChanged = false
                    },
                    enabled = state.exercise != null && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HeavyLiftsColors.AccentStrong,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add set",
                        color = LocalContentColor.current,
                        fontFamily = HeavyLiftsType.Body,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── "Done so far" Section ───────────────────────────────
            if (state.sets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Sets",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = HeavyLiftsColors.Fg1,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.sets.forEach { set ->
                        LoggedSetRow(
                            set = set,
                            deleteEnabled = !state.isSaving,
                            onDelete = { onEvent(LogWorkoutEvent.DeleteSet(set.id)) }
                        )
                    }
                }
            }
        }

        // ── Bottom Fixed Primary Action Button ("Finish lift →") ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp)
        ) {
            Button(
                onClick = {
                    val saveEvent = if (state.sets.isEmpty() || inputChanged) {
                        LogWorkoutEvent.SaveWorkout(weightValue.toString(), repsValue.toString())
                    } else {
                        LogWorkoutEvent.SaveWorkout()
                    }
                    onEvent(saveEvent)
                },
                enabled = state.exercise != null && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HeavyLiftsColors.Navy,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Finish lift",
                    color = LocalContentColor.current,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun LogTopBar(
    exerciseName: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(16.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = HeavyLiftsColors.Fg1,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = exerciseName,
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 52.sp,
            lineHeight = 56.sp,
            letterSpacing = (-2).sp
        )


    }
}

// ─── Exercise Heading ─────────────────────────────────────────────────────────

@Composable
private fun ExerciseHeading() {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    ) {

    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp),
        color = HeavyLiftsColors.Fg1,
        fontFamily = HeavyLiftsType.Display,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

// ─── Stepper Card Component ───────────────────────────────────────────────────

@Composable
private fun StepperCard(
    modifier: Modifier = Modifier,
    label: String,
    valueStr: String,
    unitStr: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(
        modifier = modifier
            .background(HeavyLiftsColors.BgOverlay, RoundedCornerShape(36.dp))
            .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = valueStr,
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 60.sp,
            lineHeight = 64.sp,
            letterSpacing = (-2).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = unitStr,
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Decrement / Increment Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Minus button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp, 10.dp, 10.dp, 28.dp))
                    .background(HeavyLiftsColors.BgChip)
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $unitStr",
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Plus button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp, 28.dp, 28.dp, 10.dp))
                    .background(HeavyLiftsColors.Accent)
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $unitStr",
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Logged Set Row ───────────────────────────────────────────────────────────

@Composable
private fun LoggedSetRow(
    set: WorkoutEntryEntity,
    deleteEnabled: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.BgOverlay, CircleShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetNumberBadge(
            number = set.sets,
            modifier = Modifier.size(48.dp),
            containerColor = HeavyLiftsColors.Accent,
            contentColor = HeavyLiftsColors.NavyStrong
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = buildAnnotatedString {
                append(set.weight.toString())
                withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)) { append(" kg") }
                append(" × ${set.reps}")
            },
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onDelete,
            enabled = deleteEnabled,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(HeavyLiftsColors.DangerSoft)
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete set",
                tint = HeavyLiftsColors.Danger,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
