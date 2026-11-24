package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetBadgeShape
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType

private data class SessionPoint(val date: String, val weight: Int, val estimatedMax: Int)

private val sampleSessions = listOf(
    SessionPoint("12 Oct", 96, 107),
    SessionPoint("21 Oct", 98, 109),
    SessionPoint("28 Oct", 101, 113),
    SessionPoint("4 Nov", 103, 116),
    SessionPoint("11 Nov", 106, 120),
    SessionPoint("18 Nov", 110, 124)
)

@Composable
internal fun ProgressTrendChart(modifier: Modifier = Modifier, estimatedMax: Boolean = false) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = HeavyLiftsColors.TextMuted,
        fontFamily = HeavyLiftsType.Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp
    )
    val latestStyle = labelStyle.copy(color = HeavyLiftsColors.AccentStrong, fontWeight = FontWeight.Bold)
    val dates = sampleSessions.mapIndexed { index, session ->
        measurer.measure(session.date, if (index == sampleSessions.lastIndex) latestStyle else labelStyle)
    }
    val values = sampleSessions.map { if (estimatedMax) it.estimatedMax else it.weight }
    val ticks = if (estimatedMax) 105..130 step 5 else 95..115 step 5
    val weights = ticks.map { measurer.measure("$it kg", labelStyle) }
    val tooltip = measurer.measure("${values.last()} kg", labelStyle.copy(color = HeavyLiftsColors.White))
    val axisWidth = weights.maxOf { it.size.width }
    val plotHeight = 86.dp
    val axisGap = 6.dp
    val density = LocalDensity.current
    val minWidth = with(density) {
        axisWidth.toDp() + axisGap + (dates.maxOf { it.size.width }.toDp() + 4.dp) * dates.size
    }
    val chartHeight = with(density) {
        plotHeight + tooltip.size.height.toDp() + dates.last().size.height.toDp() + 7.dp
    }
    val metric = if (estimatedMax) "Estimated one rep max" else "Top set weight"
    val readings = sampleSessions.mapIndexed { i, session -> "${session.date}, ${values[i]} kilograms" }

    Canvas(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .widthIn(min = minWidth)
            .height(chartHeight)
            .semantics { contentDescription = "$metric. ${readings.joinToString(". ")}. Latest personal record." }
    ) {
        val left = axisWidth + axisGap.toPx()
        val bottom = size.height - dates.last().size.height - 6.dp.toPx()
        val columnWidth = (size.width - left) / dates.size
        fun dateX(index: Int) = left + (index + 0.5f) * columnWidth
        fun weightY(kg: Int) = bottom - (kg - ticks.first) * plotHeight.toPx() / (ticks.last - ticks.first)

        val gridDash = PathEffect.dashPathEffect(floatArrayOf(3.5.dp.toPx(), 3.5.dp.toPx()))
        weights.forEachIndexed { index, label ->
            val y = weightY(ticks.first + ticks.step * index)
            drawLine(
                color = HeavyLiftsColors.AccentMuted,
                start = Offset(left, y),
                end = Offset(size.width, y),
                strokeWidth = 0.7.dp.toPx(),
                pathEffect = gridDash
            )
            val labelX = (axisWidth - label.size.width).toFloat()
            drawText(label, topLeft = Offset(labelX, y - label.size.height / 2f))
        }
        dates.forEachIndexed { index, label ->
            drawText(label, topLeft = Offset(dateX(index) - label.size.width / 2f, bottom + 6.dp.toPx()))
        }

        val points = values.mapIndexed { i, value -> Offset(dateX(i), weightY(value)) }
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val last = points.last()
        val area = Path().apply {
            addPath(line)
            lineTo(last.x, bottom)
            lineTo(points.first().x, bottom)
            close()
        }
        drawPath(
            area,
            Brush.verticalGradient(
                colors = listOf(
                    HeavyLiftsColors.Accent.copy(alpha = 0.34f),
                    HeavyLiftsColors.Accent.copy(alpha = 0.02f)
                ),
                startY = last.y,
                endY = bottom
            )
        )
        drawPath(line, HeavyLiftsColors.AccentStrong, style = Stroke(width = 1.8.dp.toPx()))
        points.dropLast(1).forEach { point ->
            drawCircle(HeavyLiftsColors.White, 3.8.dp.toPx(), point)
            drawCircle(HeavyLiftsColors.AccentStrong, 3.dp.toPx(), point)
        }
        drawRecordMarker(last, tooltip)
    }
}

private fun DrawScope.drawRecordMarker(point: Offset, label: TextLayoutResult) {
    val radius = 7.dp.toPx()
    val badge = SetBadgeShape.createOutline(Size(radius * 2, radius * 2), layoutDirection, this)
    translate(point.x - radius, point.y - radius) {
        drawOutline(badge, HeavyLiftsColors.White, style = Stroke(width = 2.dp.toPx()))
        drawOutline(badge, HeavyLiftsColors.AccentStrong)
    }

    val bubble = Size(label.size.width + 14.dp.toPx(), label.size.height + 4.dp.toPx())
    val bottom = point.y - 14.dp.toPx()
    if (bubble.width > size.width) return
    val left = (point.x - bubble.width / 2f).coerceIn(0f, size.width - bubble.width)
    val topLeft = Offset(left, bottom - bubble.height)
    drawRoundRect(HeavyLiftsColors.Navy, topLeft, bubble, CornerRadius(5.dp.toPx()))
    val pointer = Path().apply {
        moveTo(point.x - 5.dp.toPx(), bottom - 1.dp.toPx())
        lineTo(point.x, bottom + 5.dp.toPx())
        lineTo(point.x + 5.dp.toPx(), bottom - 1.dp.toPx())
        close()
    }
    drawPath(pointer, HeavyLiftsColors.Navy)
    drawText(label, topLeft = topLeft + Offset(7.dp.toPx(), 2.dp.toPx()))
}
