package ru.health.stream.feature.vitals.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.DurationUnit

internal class DatePositionTransformer(
    nowDate: Instant,
    timeZone: TimeZone,
    period: Period = Period.Day,
) {

    private val start: Instant
    private val durationSecondsInv: Double // Инвертированная длительность для замены деления умножением

    init {
        val localDate = nowDate.toLocalDateTime(timeZone).date

        start = when (period) {
            Period.Day -> localDate
            Period.Month -> LocalDate(localDate.year, localDate.month, 1)
            Period.Year -> LocalDate(localDate.year, 1, 1)
            is Period.Week -> {
                val offset = (localDate.dayOfWeek.value - period.startDayOfWeek.value)
                    .let { if (it < 0) it + 7 else it }

                localDate.minus(offset, DateTimeUnit.DAY)
            }
        }.atStartOfDayIn(timeZone)

        val end = when (period) {
            Period.Day -> localDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
            is Period.Week -> start.plus(7, DateTimeUnit.DAY, timeZone)
            Period.Month -> {
                val startOfMonth = LocalDate(localDate.year, localDate.month, 1)

                startOfMonth.plus(1, DateTimeUnit.MONTH).atStartOfDayIn(timeZone)
            }

            Period.Year -> {
                val startOfYear = LocalDate(localDate.year, 1, 1)

                startOfYear.plus(1, DateTimeUnit.YEAR).atStartOfDayIn(timeZone)
            }
        }

        val durationSeconds = (end - start).toDouble(DurationUnit.SECONDS)
        durationSecondsInv = if (durationSeconds > 0) 1.0 / durationSeconds else 0.0
    }

    fun transform(date: Instant): Float {
        val diffSeconds = (date - start).toDouble(DurationUnit.SECONDS)

        return (diffSeconds * durationSecondsInv).toFloat()
    }
}

internal sealed interface Period {

    data object Day : Period
    data class Week(val startDayOfWeek: DayOfWeek) : Period
    data object Month : Period
    data object Year : Period
}
