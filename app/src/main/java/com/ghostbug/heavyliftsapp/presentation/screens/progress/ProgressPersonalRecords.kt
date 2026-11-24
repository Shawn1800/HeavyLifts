package com.ghostbug.heavyliftsapp.presentation.screens.progress

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.domain.model.ProgressHistory
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

@Composable
internal fun ProgressPersonalRecords(history: ProgressHistory, modifier: Modifier = Modifier) {
    val heaviestSet = history.heaviestSet ?: return
    val bestVolumeSet = history.bestVolumeSet ?: return
    val bestSetVolume = history.bestSetVolume ?: return
    val mostRepsSet = history.mostRepsSet ?: return
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val weightFormat = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }
    }
    val dateFormat = remember(locale, zone) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "yMMMd"), locale)
            .withDecimalStyle(DecimalStyle.of(locale))
            .withZone(zone)
    }
    val heaviestWeight = weightFormat.format(heaviestSet.weight)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.progress_records_title),
            modifier = Modifier
                .padding(start = 8.dp, bottom = 2.dp)
                .semantics { heading() },
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        PersonalRecordRow(
            initials = stringResource(R.string.progress_record_heaviest_initials),
            title = stringResource(R.string.progress_record_heaviest),
            weight = heaviestWeight,
            reps = heaviestSet.reps,
            date = dateFormat.format(Instant.ofEpochMilli(heaviestSet.date)),
            value = stringResource(R.string.progress_weight_kg, heaviestWeight)
        )
        PersonalRecordRow(
            initials = stringResource(R.string.progress_record_volume_initials),
            title = stringResource(R.string.progress_record_volume),
            weight = weightFormat.format(bestVolumeSet.weight),
            reps = bestVolumeSet.reps,
            date = dateFormat.format(Instant.ofEpochMilli(bestVolumeSet.date)),
            value = stringResource(
                R.string.progress_weight_kg,
                weightFormat.format(bestSetVolume)
            )
        )
        PersonalRecordRow(
            initials = stringResource(R.string.progress_record_reps_initials),
            title = stringResource(R.string.progress_record_max_reps),
            weight = weightFormat.format(mostRepsSet.weight),
            reps = mostRepsSet.reps,
            date = dateFormat.format(Instant.ofEpochMilli(mostRepsSet.date)),
            value = pluralStringResource(
                R.plurals.progress_record_reps,
                mostRepsSet.reps,
                mostRepsSet.reps
            )
        )
    }
}

@Composable
private fun PersonalRecordRow(
    initials: String,
    title: String,
    weight: String,
    reps: Int,
    date: String,
    value: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        color = HeavyLiftsColors.BgOverlay,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.clearAndSetSemantics {},
                color = HeavyLiftsColors.AccentSoft,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 52.dp, minHeight = 52.dp)
                        .padding(horizontal = 8.dp),
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
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(end = 8.dp),
                        color = HeavyLiftsColors.Fg1,
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(color = HeavyLiftsColors.AccentStrong, shape = CircleShape) {
                        Text(
                            text = value,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = HeavyLiftsColors.White,
                            fontFamily = HeavyLiftsType.Body,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = stringResource(
                        R.string.progress_record_detail,
                        stringResource(R.string.progress_weight_kg, weight),
                        pluralStringResource(R.plurals.progress_record_reps, reps, reps),
                        date
                    ),
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
