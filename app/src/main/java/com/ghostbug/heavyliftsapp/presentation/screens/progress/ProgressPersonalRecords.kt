package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors

@Composable
internal fun ProgressPersonalRecords() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Personal records",
            modifier = Modifier.padding(bottom = 4.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        PersonalRecordRow("HW", "Heaviest weight", "110 kg × 3 reps · 18 Nov", "110 kg")
        PersonalRecordRow("BV", "Best set volume", "96 kg × 20 reps · 04 Nov", "1,920 kg")
        PersonalRecordRow("MR", "Max reps", "80 kg × 14 reps · 21 Oct", "14 reps")
    }
}

@Composable
private fun PersonalRecordRow(initials: String, title: String, detail: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.BgOverlay, RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {}
            .heightIn(min = 50.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 36.dp)
                .background(HeavyLiftsColors.AccentSoft, RoundedCornerShape(11.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelSmall,
                color = HeavyLiftsColors.Fg1,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp
            )
        }
        Text(
            text = value,
            modifier = Modifier
                .background(HeavyLiftsColors.AccentStrong, CircleShape)
                .padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = HeavyLiftsColors.White,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = HeavyLiftsColors.TextMuted
        )
    }
}
