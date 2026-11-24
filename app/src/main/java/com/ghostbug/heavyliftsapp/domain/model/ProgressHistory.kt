package com.ghostbug.heavyliftsapp.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class ProgressMetric { TopWeight, EstimatedOneRepMax }

enum class ProgressRange { Week, Month, Year, All }

data class ProgressChartPoint(val date: LocalDate, val weight: Float)

data class ProgressSession(
    val date: LocalDate,
    val topSet: WorkoutEntryEntity,
    val estimatedOneRepMax: Float,
    val weightRecordIncrease: Float?,
)

class ProgressHistory(
    entries: List<WorkoutEntryEntity>,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    private val sessions: List<ProgressSession>

    val heaviestSet: WorkoutEntryEntity?
    val bestVolumeSet: WorkoutEntryEntity?
    val bestSetVolume: Double?
    val mostRepsSet: WorkoutEntryEntity?
    val bestEstimatedMax: Float?
    val allTimeWeightChangePercent: Float?

    init {
        val sets = entries.filter { it.weight.isFinite() && it.weight >= 0f && it.reps > 0 }
        sessions = groupSessions(sets)
        heaviestSet = sets.maxByOrNull { it.weight }
        bestVolumeSet = sets.maxByOrNull { it.volume }
        bestSetVolume = bestVolumeSet?.volume
        mostRepsSet = sets.maxByOrNull { it.reps }
        bestEstimatedMax = sessions.maxOfOrNull { it.estimatedOneRepMax }
        allTimeWeightChangePercent = calculateAllTimeWeightChange()
    }

    fun chartPoints(
        metric: ProgressMetric,
        range: ProgressRange,
        today: LocalDate = LocalDate.now(zoneId),
    ): List<ProgressChartPoint> =
        visibleSessions(range, today).map { session ->
            val weight = when (metric) {
                ProgressMetric.TopWeight -> session.topSet.weight
                ProgressMetric.EstimatedOneRepMax -> session.estimatedOneRepMax
            }
            ProgressChartPoint(session.date, weight)
        }.toList()

    fun latestSession(
        range: ProgressRange,
        today: LocalDate = LocalDate.now(zoneId),
    ): ProgressSession? = visibleSessions(range, today).lastOrNull()

    private fun visibleSessions(range: ProgressRange, today: LocalDate): Sequence<ProgressSession> {
        val start = when (range) {
            ProgressRange.Week -> today.minusDays(6)
            ProgressRange.Month -> today.minusMonths(1)
            ProgressRange.Year -> today.minusYears(1)
            ProgressRange.All -> return sessions.asSequence()
        }
        val dateWindow = start..today
        return sessions.asSequence().filter { it.date in dateWindow }
    }

    private fun calculateAllTimeWeightChange(): Float? {
        val firstWeight = sessions.firstOrNull()?.topSet?.weight ?: return null
        val bestWeight = heaviestSet?.weight ?: return null
        if (firstWeight == 0f || sessions.size < 2) return null
        val percent = ((bestWeight.toDouble() - firstWeight) / firstWeight) * 100
        return percent.toFloat().takeIf { it.isFinite() }
    }

    private fun groupSessions(entries: List<WorkoutEntryEntity>): List<ProgressSession> {
        val setsByDay = entries.groupBy { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
        var previousBest: Float? = null
        return setsByDay.toSortedMap().map { (date, sets) ->
            val topSet = sets.maxBy { it.weight }
            val recordIncrease = previousBest?.let { topSet.weight - it }?.takeIf { it > 0f }
            previousBest = maxOf(previousBest ?: topSet.weight, topSet.weight)

            ProgressSession(
                date = date,
                topSet = topSet,
                estimatedOneRepMax = sets.maxOf { it.estimatedOneRepMax() }.coerceAtMost(Float.MAX_VALUE),
                weightRecordIncrease = recordIncrease,
            )
        }
    }
}

private val WorkoutEntryEntity.volume: Double
    get() = weight.toDouble() * reps
