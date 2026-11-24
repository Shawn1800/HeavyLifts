package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetBadgeShape
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors

private val metrics = listOf("Top Weight", "Est. 1RM")
private val ranges = listOf("1W", "1M", "1Y", "All")

@Composable
fun ProgressScreen(
    progressViewModel: ProgressViewModel,

    modifier: Modifier = Modifier
) {

    val state by progressViewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HeavyLiftsColors.Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        ExerciseHeader()

        Spacer(Modifier.height(10.dp))

        PersonalBestCard(
            event = progressViewModel::onEvent,
            state = state,
        )
        Spacer(Modifier.height(10.dp))
        ProgressionCard()
        Spacer(Modifier.height(12.dp))
        ProgressPersonalRecords()
    }
}

@Composable
private fun ExerciseHeader(
) {
    var menuOpen by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 4.dp)) {
        Text(
            "CHEST · EXERCISE PROGRESS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = HeavyLiftsColors.AccentStrong
        )
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClickLabel = "Select exercise") {
                        menuOpen = true
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Bench Press",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(22.dp), tint = HeavyLiftsColors.TextMuted)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Bench Press") },
                    trailingIcon = { Icon(Icons.Default.Check, contentDescription = "Selected") },
                    onClick = { menuOpen = false }
                )
            }
        }
    }
}

@Composable
private fun PersonalBestCard(
    event: (ProgressEvent) -> Unit,
    state: ProgressState
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.Accent, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    "ALL-TIME PR", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    buildAnnotatedString {
                        val weight = state.maxWeight
                        if (weight == null) {
                            append("—")
                        } else {
                            append(weight.toString())
                            withStyle(SpanStyle(fontSize = 17.sp, letterSpacing = 0.sp)) {
                                append(" kg")
                            }
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 17.sp,
                                letterSpacing = 0.sp
                            )
                        ) { append(" kg") }
                    },
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 49.sp
                )
            }
            Box(
                Modifier
                    .size(44.dp)
                    .background(HeavyLiftsColors.AccentStrong, SetBadgeShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.EmojiEvents,
                    null,
                    Modifier.size(22.dp),
                    tint = HeavyLiftsColors.White
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SummaryPill("+15.8% all time", HeavyLiftsColors.AccentStrong)
            SummaryPill("Est. 1RM 124 kg", HeavyLiftsColors.Fg1)
        }
    }
}

@Composable
private fun SummaryPill(text: String, color: Color) {
    Text(
        text,
        modifier = Modifier
            .background(HeavyLiftsColors.White, CircleShape)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall, fontSize = 13.sp,
        fontWeight = FontWeight.Bold, color = color
    )
}

@Composable
private fun ProgressionCard() {
    var metric by rememberSaveable { mutableStateOf(metrics.first()) }
    var range by rememberSaveable { mutableStateOf("1M") }
    val estimated = metric == "Est. 1RM"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(end = 4.dp)) {
                Text(
                    "Progression", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (estimated) "Est. 1RM · Last 6 sessions" else "Top set weight · Last 6 sessions",
                    style = MaterialTheme.typography.bodySmall, fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            ProgressSelector(metrics, metric, onSelect = { metric = it })
        }
        Spacer(Modifier.height(10.dp))
        ProgressSelector(ranges, range, onSelect = { range = it }, fillWidth = true)
        Spacer(Modifier.height(10.dp))
        LatestSessionStrip()
        Spacer(Modifier.height(8.dp))
        ProgressTrendChart(estimatedMax = estimated)
    }
}

@Composable
private fun ProgressSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    fillWidth: Boolean = false
) {
    Row(
        modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .background(HeavyLiftsColors.White, CircleShape)
            .padding(2.dp)
            .selectableGroup()
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                modifier = (if (fillWidth) Modifier.weight(1f) else Modifier)
                    .clip(CircleShape)
                    .background(if (active) HeavyLiftsColors.AccentStrong else Color.Transparent)
                    .selectable(active, role = Role.Tab) { onSelect(option) }
                    .heightIn(min = 22.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option, style = MaterialTheme.typography.labelSmall,
                    color = if (active) HeavyLiftsColors.White else HeavyLiftsColors.TextMuted,
                    fontSize = 11.sp, letterSpacing = 0.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LatestSessionStrip() {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = HeavyLiftsColors.AccentStrong)) { append("18 NOV") }
                withStyle(SpanStyle(color = HeavyLiftsColors.Outline)) { append("    •    ") }
                append("110 kg × 3 reps")
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "+2.5 kg PR",
            modifier = Modifier
                .background(HeavyLiftsColors.AccentSoft, CircleShape)
                .padding(horizontal = 8.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
            color = HeavyLiftsColors.AccentStrong
        )
    }
}

//@Preview(showBackground = true, widthDp = 393, heightDp = 740)
//@Composable
//private fun ProgressScreenPreview() {
//    Demo103Theme { ProgressScreen(
//
//    ) }
//}
