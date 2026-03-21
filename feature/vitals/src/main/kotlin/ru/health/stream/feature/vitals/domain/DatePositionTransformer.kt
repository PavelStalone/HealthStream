package ru.health.stream.feature.vitals.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.health.stream.feature.vitals.data.model.Period
import kotlin.time.DurationUnit

internal class DatePositionTransformer(
    dateNow: Instant,
    timeZone: TimeZone,
    period: Period = Period.Day,
) {

    private val start: Instant
    private val durationSecondsInv: Double // Инвертированная длительность для замены деления умножением

    init {
        val range = period.calculateRange(date = dateNow, timeZone = timeZone)

        start = range.start

        val durationSeconds = (range.endInclusive - range.start).toDouble(DurationUnit.SECONDS)
        durationSecondsInv = if (durationSeconds > 0) 1.0 / durationSeconds else 0.0
    }

    fun transform(date: Instant): Float {
        val diffSeconds = (date - start).toDouble(DurationUnit.SECONDS)

        return (diffSeconds * durationSecondsInv).toFloat()
    }
}
