package ru.health.stream.data.vitals.usecase

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.health.stream.data.vitals.model.Period
import kotlin.time.DurationUnit

class DateTransformerUseCase(
    timeZone: TimeZone,
    period: Period = Period.Day,
    dateRange: ClosedRange<Instant>,
) {

    private val start: Instant
    private val durationSecondsInv: Double // Инвертированная длительность для замены деления умножением

    init {
        start = period.calculateRange(date = dateRange.start, timeZone = timeZone).start
        val end = period.calculateRange(date = dateRange.endInclusive, timeZone = timeZone).endInclusive

        val durationSeconds = (end - start).toDouble(DurationUnit.SECONDS)
        durationSecondsInv = if (durationSeconds > 0) 1.0 / durationSeconds else 0.0
    }

    operator fun invoke(date: Instant): Float {
        val diffSeconds = (date - start).toDouble(DurationUnit.SECONDS)

        return (diffSeconds * durationSecondsInv).toFloat()
    }
}
