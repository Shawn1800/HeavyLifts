package com.ghostbug.heavyliftsapp.presentation.screens.progress

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.domain.model.ProgressChartPoint
import com.ghostbug.heavyliftsapp.presentation.ui.components.SetBadgeShape
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
internal fun ProgressTrendChart(
    points: List<ProgressChartPoint>,
    metricLabel: String,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val first = points.first()
    val latest = points.last()
    val lowest = points.minOf { it.weight }.toDouble()
    val highest = points.maxOf { it.weight }.toDouble()
    val ticks = weightTicks(lowest, highest)

    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }
    val weightFormat = stringResource(R.string.progress_weight_kg)
    fun weightLabel(weight: Double) = weightFormat.format(locale, numberFormat.format(weight))

    val shortDate = DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(locale, if (first.date.year == latest.date.year) "MMMd" else "yMMMd"),
        locale,
    )
    val spokenDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    val description = if (points.size == 1) {
        stringResource(
            R.string.progress_chart_single_session,
            metricLabel,
            latest.date.format(spokenDate),
            weightLabel(latest.weight.toDouble()),
        )
    } else {
        stringResource(
            R.string.progress_chart_summary,
            metricLabel,
            first.date.format(spokenDate),
            latest.date.format(spokenDate),
            weightLabel(lowest),
            weightLabel(highest),
            weightLabel(latest.weight.toDouble()),
        )
    }

    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall.copy(color = HeavyLiftsColors.Fg3)
    val firstDate = measurer.measure(first.date.format(shortDate), labelStyle)
    val latestDate = measurer.measure(
        latest.date.format(shortDate),
        labelStyle.copy(color = HeavyLiftsColors.NavyStrong, fontWeight = FontWeight.Bold),
    )
    val weightLabels = ticks.map { measurer.measure(weightLabel(it), labelStyle) }
    val latestLabel = measurer.measure(
        weightLabel(latest.weight.toDouble()),
        labelStyle.copy(color = HeavyLiftsColors.White, fontWeight = FontWeight.Bold),
    )
    val axisWidth = weightLabels.maxOf { it.size.width }
    val dateHeight = maxOf(firstDate.size.height, latestDate.size.height)
    val minWidth: Dp
    val height: Dp
    with(LocalDensity.current) {
        val dateWidth = if (points.size == 1) latestDate.size.width else firstDate.size.width + latestDate.size.width
        minWidth = (axisWidth + maxOf(dateWidth, latestLabel.size.width)).toDp() + 32.dp
        val tickSpacing = weightLabels.maxOf { it.size.height }.toDp() + 6.dp
        val plotHeight = maxOf(112.dp, tickSpacing * ticks.lastIndex)
        height = plotHeight + (latestLabel.size.height + dateHeight).toDp() + 36.dp
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chartWidth = if (constraints.hasBoundedWidth) maxOf(maxWidth, minWidth) else minWidth
        Box(Modifier.horizontalScroll(rememberScrollState())) {
            Canvas(
                modifier = Modifier
                    .width(chartWidth)
                    .height(height)
                    .semantics { contentDescription = description },
            ) {
                val gridLeft = axisWidth + 8.dp.toPx()
                val plotLeft = gridLeft + 8.dp.toPx()
                val plotRight = size.width - 8.dp.toPx()
                val plotTop = latestLabel.size.height + 26.dp.toPx()
                val plotBottom = size.height - dateHeight - 10.dp.toPx()
                if (plotRight <= plotLeft || plotBottom <= plotTop) return@Canvas

                val axisMin = ticks.first()
                val axisRange = ticks.last() - axisMin
                val firstDay = first.date.toEpochDay()
                val elapsedDays = latest.date.toEpochDay() - firstDay
                fun weightY(weight: Double): Float {
                    val fraction = ((weight - axisMin) / axisRange).toFloat()
                    return plotBottom - (plotBottom - plotTop) * fraction
                }

                val gridDash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                weightLabels.forEachIndexed { index, label ->
                    val y = weightY(ticks[index])
                    drawLine(
                        color = HeavyLiftsColors.BorderSubtle,
                        start = Offset(gridLeft, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = gridDash,
                    )
                    drawText(label, topLeft = Offset((axisWidth - label.size.width).toFloat(), y - label.size.height / 2f))
                }

                val positions = points.map { point ->
                    val fraction = if (elapsedDays == 0L) 0.5f else (point.date.toEpochDay() - firstDay).toFloat() / elapsedDays
                    Offset(plotLeft + (plotRight - plotLeft) * fraction, weightY(point.weight.toDouble()))
                }
                val lastPoint = positions.last()
                val dateTop = plotBottom + 10.dp.toPx()
                if (elapsedDays == 0L) {
                    drawText(latestDate, topLeft = Offset(lastPoint.x - latestDate.size.width / 2f, dateTop))
                } else {
                    drawText(firstDate, topLeft = Offset(gridLeft, dateTop))
                    drawText(latestDate, topLeft = Offset(size.width - latestDate.size.width, dateTop))
                }

                if (positions.size > 1) {
                    val start = positions.first()
                    val line = Path().apply {
                        moveTo(start.x, start.y)
                        for (index in 1..positions.lastIndex) {
                            val position = positions[index]
                            lineTo(position.x, position.y)
                        }
                    }
                    val area = Path().apply {
                        addPath(line)
                        lineTo(lastPoint.x, plotBottom)
                        lineTo(start.x, plotBottom)
                        close()
                    }
                    drawPath(
                        path = area,
                        brush = Brush.verticalGradient(
                            colors = listOf(HeavyLiftsColors.AccentSoft, HeavyLiftsColors.AccentSoft.copy(alpha = 0.12f)),
                            startY = plotTop,
                            endY = plotBottom,
                        ),
                    )
                    drawPath(
                        path = line,
                        color = HeavyLiftsColors.AccentStrong,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
                drawLatestReading(lastPoint, latestLabel)
            }
        }
    }
}

private fun weightTicks(lowest: Double, highest: Double): List<Double> {
    val minimumInterval = maxOf(0.5, Math.ulp(highest) * 4)
    val interval = ((highest - lowest) / 3).coerceAtLeast(minimumInterval)
    val magnitude = 10.0.pow(floor(log10(interval)))
    val step = ceil(interval / magnitude) * magnitude
    val firstTick = floor(lowest / step)
    val lastTick = ceil(highest / step)
    val steps = (lastTick - firstTick).roundToInt().coerceAtLeast(2)
    return List(steps + 1) { (firstTick + it) * step }
}

private fun DrawScope.drawLatestReading(point: Offset, label: TextLayoutResult) {
    val radius = 7.dp.toPx()
    val badge = SetBadgeShape.createOutline(Size(radius * 2, radius * 2), layoutDirection, this)
    translate(point.x - radius, point.y - radius) {
        drawOutline(badge, HeavyLiftsColors.White, style = Stroke(width = 2.dp.toPx()))
        drawOutline(badge, HeavyLiftsColors.AccentStrong)
    }

    val bubble = Size(label.size.width + 16.dp.toPx(), label.size.height + 8.dp.toPx())
    if (bubble.width > size.width) return

    val bottom = point.y - 12.dp.toPx()
    val left = (point.x - bubble.width / 2f).coerceIn(0f, size.width - bubble.width)
    val topLeft = Offset(left, bottom - bubble.height)
    drawRoundRect(HeavyLiftsColors.Navy, topLeft, bubble, CornerRadius(8.dp.toPx()))
    drawText(label, topLeft = topLeft + Offset(8.dp.toPx(), 4.dp.toPx()))
}
