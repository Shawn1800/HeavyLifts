package com.ghostbug.heavyliftsapp.presentation.screens.log_workout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    var currentWeight by remember { mutableStateOf("") }
    var currentReps by remember { mutableStateOf("") }

    LaunchedEffect(state.exercise?.id) {
        currentWeight = ""
        currentReps = ""
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeavyLiftsColors.Bg)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 112.dp)
        ) {
            LogTopBar(onBack = onBack)

            ExerciseHeading(exerciseName = state.exercise?.exerciseName.orEmpty())

            SectionHeading(text = "Add set ${state.sets.size + 1}")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(HeavyLiftsColors.BgElevated)
                    .padding(14.dp)
            ) {
                SetInputRow(
                    setNumber = state.sets.size + 1,
                    weight = currentWeight,
                    reps = currentReps,
                    badgeColor = HeavyLiftsColors.AccentStrong,
                    onWeightChange = { currentWeight = it },
                    onRepsChange = { currentReps = it },
                    onDelete = {
                        currentWeight = ""
                        currentReps = ""
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onEvent(
                            LogWorkoutEvent.OnAddingSets(
                                exerciseId = state.exercise?.id ?: 0,
                                weight = currentWeight,
                                reps = currentReps
                            )
                        )
                        currentWeight = ""
                        currentReps = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HeavyLiftsColors.AccentSoft,
                        contentColor = HeavyLiftsColors.NavyStrong
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Add set",
                        fontFamily = HeavyLiftsType.Body,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.sets.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Logged sets",
                        color = HeavyLiftsColors.Fg1,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${state.sets.size} total",
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HeavyLiftsColors.BgElevated)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        color = HeavyLiftsColors.Fg2,
                        fontFamily = HeavyLiftsType.Body,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.sets.forEach { set ->
                        LoggedSetRow(
                            set = set,
                            onDelete = { onEvent(LogWorkoutEvent.DeleteSet(set.id)) }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onEvent(LogWorkoutEvent.SaveWorkout(currentWeight, currentReps)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp),
            icon = {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
            },
            text = {
                Text(
                    text = "Save",
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = HeavyLiftsColors.Accent,
            contentColor = HeavyLiftsColors.NavyStrong,
            shape = RoundedCornerShape(28.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        )
    }
}

@Composable
private fun LogTopBar(onBack: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .background(HeavyLiftsColors.BgElevated, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = HeavyLiftsColors.Navy,
                modifier = Modifier.size(24.dp)
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(HeavyLiftsColors.BgElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = HeavyLiftsColors.Navy,
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Close session") },
                    onClick = {
                        menuExpanded = false
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExerciseHeading(exerciseName: String) {
    Column(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp)
    ) {
        Text(
            text = "LOG",
            color = HeavyLiftsColors.AccentStrong,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = exerciseName,
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            lineHeight = 52.sp,
            maxLines = 2
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 13.dp, bottom = 5.dp),
        color = HeavyLiftsColors.Fg1,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun SetInputRow(
    setNumber: Int,
    weight: String,
    reps: String,
    badgeColor: Color,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDelete: () -> Unit,
    readOnly: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetNumberBadge(
            number = setNumber,
            modifier = Modifier.size(56.dp),
            containerColor = badgeColor
        )
        WeightRepsField(
            value = weight,
            placeholder = "Kg",
            onValueChange = onWeightChange,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            readOnly = readOnly,
            modifier = Modifier.weight(1f)
        )
        WeightRepsField(
            value = reps,
            placeholder = "Reps",
            onValueChange = onRepsChange,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            readOnly = readOnly,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(HeavyLiftsColors.DangerSoft)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete set",
                tint = HeavyLiftsColors.Danger,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LoggedSetRow(
    set: WorkoutEntryEntity,
    onDelete: () -> Unit
) {
    var weight by remember(set.id) { mutableStateOf(set.weight.toString()) }
    var reps by remember(set.id) { mutableStateOf(set.reps.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(HeavyLiftsColors.BgOverlay)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetNumberBadge(
            number = set.sets,
            modifier = Modifier.size(56.dp),
            containerColor = HeavyLiftsColors.Accent,
            contentColor = HeavyLiftsColors.NavyStrong
        )
        Spacer(modifier = Modifier.width(10.dp))
        WeightRepsField(
            value = weight,
            placeholder = "",
            label = "KG",
            onValueChange = { weight = it },
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            readOnly = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        WeightRepsField(
            value = reps,
            placeholder = "",
            label = "REPS",
            onValueChange = { reps = it },
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            readOnly = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(HeavyLiftsColors.DangerSoft)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete set",
                tint = HeavyLiftsColors.Danger,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun WeightRepsField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    readOnly: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(56.dp),
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        label = label?.let { fieldLabel ->
            {
                Text(
                    text = fieldLabel,
                    color = HeavyLiftsColors.Fg2,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        placeholder = if (placeholder.isNotEmpty()) {
            {
            Text(
                text = placeholder,
                color = HeavyLiftsColors.Fg2,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 16.sp
            )
            }
        } else null,
        textStyle = MaterialTheme.typography.titleMedium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HeavyLiftsColors.Fg1,
            unfocusedTextColor = HeavyLiftsColors.Fg1,
            focusedBorderColor = HeavyLiftsColors.Outline,
            unfocusedBorderColor = HeavyLiftsColors.Outline,
            focusedLabelColor = HeavyLiftsColors.Fg2,
            unfocusedLabelColor = HeavyLiftsColors.Fg2,
            focusedPlaceholderColor = HeavyLiftsColors.Fg2,
            unfocusedPlaceholderColor = HeavyLiftsColors.Fg2,
            cursorColor = HeavyLiftsColors.AccentStrong,
            focusedContainerColor = HeavyLiftsColors.BgElevated,
            unfocusedContainerColor = HeavyLiftsColors.BgElevated,
            disabledContainerColor = HeavyLiftsColors.BgElevated
        )
    )
}
