package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyliftsapp.domain.model.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.presentation.screens.progress.ExerciseProgressSection
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetNumberBadge
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch
import java.math.BigDecimal

private val WeightInputPattern = Regex("[0-9]*(?:[.,][0-9]*)?")
private val WeightStep = BigDecimal("0.5")

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
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LogWorkoutContent(
            state = state,
            onBack = onBack,
            onEvent = logWorkoutViewModel::onEvent,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
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
    var weightValue by remember { mutableStateOf(TextFieldValue("1.0")) }
    var repsValue by remember { mutableStateOf(TextFieldValue("20")) }
    var inputChanged by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val repsFocusRequester = remember { FocusRequester() }
    val canEdit = state.exercise != null && !state.isSaving

    LaunchedEffect(state.exercise?.id, state.dateMillis) {
        val lastSet = state.sets.lastOrNull()
        weightValue = TextFieldValue(lastSet?.weight?.toString() ?: "1.0")
        repsValue = TextFieldValue(lastSet?.reps?.toString() ?: "20")
        inputChanged = false
    }

    fun updateWeight(value: TextFieldValue) {
        if (WeightInputPattern.matches(value.text)) {
            inputChanged = inputChanged || value.text != weightValue.text
            weightValue = value
        }
    }

    fun updateReps(value: TextFieldValue) {
        if (value.text.all { it in '0'..'9' }) {
            inputChanged = inputChanged || value.text != repsValue.text
            repsValue = value
        }
    }

    fun stepWeight(delta: BigDecimal) {
        val current = weightValue.text.replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
        val next = current + delta
        if (next.signum() > 0) {
            val text = next.toPlainString()
            updateWeight(TextFieldValue(text, selection = TextRange(text.length)))
        }
    }

    fun stepReps(delta: Int) {
        val current = repsValue.text.toIntOrNull() ?: 0
        val next = current.toLong() + delta
        if (next in 1L..Int.MAX_VALUE.toLong()) {
            val text = next.toString()
            updateReps(TextFieldValue(text, selection = TextRange(text.length)))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
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
                    value = weightValue,
                    onValueChange = ::updateWeight,
                    fieldLabel = "Weight in kilograms",
                    unitStr = "kg",
                    enabled = canEdit,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { repsFocusRequester.requestFocus() }),
                    onDecrement = { stepWeight(-WeightStep) },
                    onIncrement = { stepWeight(WeightStep) }
                )

                // Reps Stepper Card
                StepperCard(
                    modifier = Modifier.weight(1f),
                    label = "REPS",
                    value = repsValue,
                    onValueChange = ::updateReps,
                    fieldLabel = "Reps",
                    unitStr = "reps",
                    enabled = canEdit,
                    fieldModifier = Modifier.focusRequester(repsFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    onDecrement = { stepReps(-1) },
                    onIncrement = { stepReps(1) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── "Lock in set" Primary Button ────────────────────────
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        val exercise = state.exercise ?: return@Button
                        val weight = weightValue.text
                        val reps = repsValue.text
                        focusManager.clearFocus()
                        onEvent(LogWorkoutEvent.OnAddingSets(exercise.id, weight, reps))
                        if (parseSetInput(weight, reps) != null) inputChanged = false
                    },
                    enabled = canEdit,
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

            if (state.exercise != null) {
                ExerciseProgressSection(
                    progress = state.progress,
                    onRetry = { onEvent(LogWorkoutEvent.RetryProgress) },
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp)
                )
            }
        }

        // ── Bottom Fixed Primary Action Button ("Finish lift →") ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val saveEvent = if (state.sets.isEmpty() || inputChanged) {
                        LogWorkoutEvent.SaveWorkout(weightValue.text, repsValue.text)
                    } else {
                        LogWorkoutEvent.SaveWorkout()
                    }
                    onEvent(saveEvent)
                },
                enabled = canEdit,
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
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    fieldLabel: String,
    unitStr: String,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    fieldModifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(focused) {
        if (focused) {
            onValueChange(value.copy(selection = TextRange(0, value.text.length)))
        }
    }

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

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier
                .fillMaxWidth()
                .background(HeavyLiftsColors.BgChip, RoundedCornerShape(14.dp))
                .onFocusChanged { focused = it.isFocused }
                .padding(horizontal = 6.dp, vertical = 8.dp)
                .semantics { contentDescription = fieldLabel },
            enabled = enabled,
            textStyle = TextStyle(
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 38.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            cursorBrush = SolidColor(HeavyLiftsColors.NavyStrong)
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
                    .clickable(enabled = enabled, onClick = onDecrement),
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
                    .clickable(enabled = enabled, onClick = onIncrement),
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
