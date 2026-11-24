package com.ghostbug.heavyliftsapp.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressHistoryTest {
    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun chartRangesUseSessionDatesAndIncludeToday() {
        val today = LocalDate.of(2026, 4, 12)
        val dates = listOf(
            today.minusYears(2),
            today.minusMonths(2),
            today.minusDays(7),
            today.minusDays(6),
            today,
        )
        val history = ProgressHistory(dates.reversed().map { workout(it, 80f, 1) }, zoneId)

        fun datesFor(range: ProgressRange) =
            history.chartPoints(ProgressMetric.TopWeight, range, today).map { it.date }

        assertEquals(dates.takeLast(2), datesFor(ProgressRange.Week))
        assertEquals(dates.takeLast(3), datesFor(ProgressRange.Month))
        assertEquals(dates.takeLast(4), datesFor(ProgressRange.Year))
        assertEquals(dates, datesFor(ProgressRange.All))
    }

    @Test
    fun bestRecordsAndEstimatedMaxUseTheCorrectSets() {
        val date = LocalDate.of(2026, 4, 12)
        val firstSession = workout(date.minusDays(1), 100f, 1)
        val heaviest = workout(date, 110f, 1)
        val betterEstimate = workout(date, 100f, 6, sets = 10)
        val mostReps = workout(date, 50f, 20)
        val history = ProgressHistory(listOf(heaviest, mostReps, firstSession, betterEstimate), zoneId)

        assertEquals(heaviest, history.heaviestSet)
        assertEquals(mostReps, history.bestVolumeSet)
        assertEquals(mostReps, history.mostRepsSet)
        assertEquals(1_000.0, history.bestSetVolume!!, 0.001)
        assertEquals(120f, history.bestEstimatedMax!!, 0.001f)
        assertEquals(10f, history.allTimeWeightChangePercent!!, 0.001f)
        assertEquals(10f, history.latestSession(ProgressRange.All, date)?.weightRecordIncrease ?: 0f, 0.001f)
        val topWeightPoints = history.chartPoints(ProgressMetric.TopWeight, ProgressRange.All, date)
        assertEquals(listOf(100f, 110f), topWeightPoints.map { it.weight })

        val estimatedPoints = history.chartPoints(ProgressMetric.EstimatedOneRepMax, ProgressRange.All, date)
        assertEquals(listOf(date.minusDays(1), date), estimatedPoints.map { it.date })
        assertEquals(100f, estimatedPoints.first().weight, 0.001f)
        assertEquals(120f, estimatedPoints.last().weight, 0.001f)
    }

    @Test
    fun recentImprovementBelowPriorBestDoesNotShowRecordIncrease() {
        val date = LocalDate.of(2026, 4, 12)
        val history = ProgressHistory(
            listOf(
                workout(date.minusDays(2), 100f, 1),
                workout(date.minusDays(1), 80f, 3),
                workout(date, 90f, 2),
            ),
            zoneId,
        )

        assertNull(history.latestSession(ProgressRange.All, date)?.weightRecordIncrease)
    }

    @Test
    fun localSessionDateAndLatestVisibleSessionUseTheSameRange() {
        val today = LocalDate.of(2026, 4, 12)
        val todaySession = workout(today, 80f, 3).copy(
            date = Instant.parse("2026-04-11T22:00:00Z").toEpochMilli(),
        )
        val tomorrowSession = workout(today.plusDays(1), 100f, 3)
        val history = ProgressHistory(listOf(todaySession, tomorrowSession), zoneId)

        val weekPoints = history.chartPoints(ProgressMetric.TopWeight, ProgressRange.Week, today)
        assertEquals(listOf(ProgressChartPoint(today, 80f)), weekPoints)
        assertEquals(todaySession, history.latestSession(ProgressRange.Week, today)?.topSet)
        assertEquals(tomorrowSession, history.latestSession(ProgressRange.All, today)?.topSet)
    }

    @Test
    fun invalidSetsAreExcludedWithoutDiscardingBodyweightSets() {
        val today = LocalDate.of(2026, 4, 12)
        val bodyweight = workout(today, 0f, 5)
        val history = ProgressHistory(
            listOf(workout(today, Float.NaN, 10), workout(today, 20f, 0), bodyweight),
            zoneId,
        )

        val points = history.chartPoints(ProgressMetric.TopWeight, ProgressRange.All, today)
        assertEquals(bodyweight, history.heaviestSet)
        assertEquals(listOf(ProgressChartPoint(today, 0f)), points)
        assertNull(history.allTimeWeightChangePercent)
    }

    private fun workout(date: LocalDate, weight: Float, reps: Int, sets: Int = 1) = WorkoutEntryEntity(
        exerciseId = 1L,
        weight = weight,
        reps = reps,
        sets = sets,
        date = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        userId = "test-user",
    )
}
