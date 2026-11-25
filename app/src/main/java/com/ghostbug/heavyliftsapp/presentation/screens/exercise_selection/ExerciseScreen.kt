package com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyliftsapp.domain.model.ExerciseEntity
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ExerciseSelectionScreen(
    viewModel: ExerciseViewModel = viewModel(),
    onNavigateToLogWorkout: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExerciseUiEvent.AddExercise  -> onNavigateToLogWorkout(event.exercise.id)
                is ExerciseUiEvent.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ExerciseSelectionContent(
                state = state,
                onBack = onBack,
                onEvent = { viewModel.onEvent(it) }
            )
        }
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
fun ExerciseSelectionContent(
    state: ExerciseState,
    onBack: () -> Unit,
    onEvent: (ExerciseEvent) -> Unit
) {
    ExerciseSelectionLayout(
        query = state.searchQuery,
        isSearching = state.isSearching,
        exercises = state.exercises,
        onBack = onBack,
        onQueryChange = { onEvent(ExerciseEvent.OnSearchQueryChange(it)) },
        onAddExercise = { onEvent(ExerciseEvent.OnAddExercise(it)) }
    )
}

// ─── Main Layout ──────────────────────────────────────────────────────────────

@Composable
fun ExerciseSelectionLayout(
    query: String,
    isSearching: Boolean,
    exercises: List<ExerciseEntity>,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onAddExercise: (ExerciseEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeavyLiftsColors.Bg),
    ) {
        SelectionTopBar(onBack = onBack)

        // ── Search field ──────────────────────────────────────────
        ExerciseSearchField(
            query = query,
            onQueryChange = onQueryChange
        )

        ExerciseSectionHeader(
            count = if (!isSearching) exercises.size else null
        )

        // ── Body ──────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                isSearching -> ExerciseLoadingIndicator()
                exercises.isEmpty() -> ExerciseEmptyState(hasQuery = query.isNotEmpty())
                else -> ExerciseList(
                    exercises = exercises,
                    onClickExercise = onAddExercise
                )
            }
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun SelectionTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding( horizontal = 16.dp , vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        Text(
            text = "Choose exercise",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            letterSpacing = (-0.7).sp,
            lineHeight = 46.sp
        )
    }
}

// ─── Search Field ─────────────────────────────────────────────────────────────

@Composable
private fun ExerciseSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        placeholder = {
            Text(
                text = "Search exercises",
                color = HeavyLiftsColors.Fg3,
                fontSize = 15.sp,
                fontFamily = HeavyLiftsType.Body
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = HeavyLiftsColors.Fg2,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = HeavyLiftsColors.Fg2,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HeavyLiftsColors.Fg1,
            unfocusedTextColor = HeavyLiftsColors.Fg1,
            focusedBorderColor = HeavyLiftsColors.AccentStrong,
            unfocusedBorderColor = HeavyLiftsColors.BorderSubtle,
            focusedPlaceholderColor = HeavyLiftsColors.Fg3,
            unfocusedPlaceholderColor = HeavyLiftsColors.Fg3,
            cursorColor = HeavyLiftsColors.AccentStrong,
            focusedContainerColor = HeavyLiftsColors.BgOverlay,
            unfocusedContainerColor = HeavyLiftsColors.BgElevated,
        )
    )
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun ExerciseSectionHeader(count: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Exercises",
            color = HeavyLiftsColors.Fg1,
            style = MaterialTheme.typography.titleLarge
        )

        count?.let {
            Text(
                text = "$it",
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = HeavyLiftsColors.Fg2,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── Exercise List ────────────────────────────────────────────────────────────

@Composable
fun ExerciseList(
    exercises: List<ExerciseEntity>,
    onClickExercise: (ExerciseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        items(items = exercises, key = { it.id }) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onClickExercise = { onClickExercise(exercise) }
            )
        }
    }
}

// ─── Exercise Item ────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCard(
    exercise: ExerciseEntity,
    onClickExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(HeavyLiftsColors.BgElevated)
            .clickable { onClickExercise() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(HeavyLiftsColors.AccentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = HeavyLiftsColors.NavyStrong,
                modifier = Modifier.size(25.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.exerciseName,
                color = HeavyLiftsColors.Fg1,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = HeavyLiftsType.Display,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = exercise.category,
                color = HeavyLiftsColors.Fg3,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = HeavyLiftsType.Body
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(HeavyLiftsColors.AccentStrong),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = HeavyLiftsColors.White,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = HeavyLiftsColors.AccentStrong,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Searching…",
                color = HeavyLiftsColors.Fg2,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun ExerciseEmptyState(hasQuery: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(HeavyLiftsColors.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasQuery) Icons.Default.Search else Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HeavyLiftsColors.NavyStrong,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (hasQuery) "No matches" else "No exercises yet",
                color = HeavyLiftsColors.Fg1,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasQuery) "Try a different search term." else "Exercises will appear here when available.",
                color = HeavyLiftsColors.Fg2,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(HeavyLiftsColors.DangerSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HeavyLiftsColors.Danger,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Something went wrong",
                color = HeavyLiftsColors.Fg1,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = HeavyLiftsColors.Fg2,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                lineHeight = 20.sp
            )
        }
    }
}
