package ru.health.stream.feature.vitals.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

internal class DatePositionTransformer(
    timeZone: TimeZone,
    nowDate: Instant,
    period: Period = Period.Day,
) {

    private val localDate = nowDate.toLocalDateTime(timeZone).date

    private val start = when (period) {
        Period.Day -> localDate.atStartOfDayIn(timeZone)
        Period.Year -> localDate.atStartOfDayIn(timeZone).minus(localDate.dayOfYear.days)
        Period.Month -> localDate.atStartOfDayIn(timeZone).minus(localDate.dayOfMonth.days)
        is Period.Week -> {
            val currentDayOfWeek = localDate.dayOfWeek
            val instant = localDate.atStartOfDayIn(timeZone)

            val offset = currentDayOfWeek.value - period.startDayOfWeek.value

            instant.minus((offset + if (offset < 0) DayOfWeek.entries.size else 0).days)
        }
    }

    private val durationSeconds = when (period) {
        Period.Day -> 1.days
        is Period.Week -> 7.days
        Period.Month -> localDate.month.maxLength().days
        Period.Year -> localDate.toJavaLocalDate().lengthOfYear().days
    }.inWholeSeconds

    fun transform(date: Instant): Float {
        return ((date - start).inWholeSeconds.toDouble() / durationSeconds).toFloat()
    }
}

internal sealed interface Period {

    data object Day : Period
    data class Week(val startDayOfWeek: DayOfWeek) : Period
    data object Month : Period
    data object Year : Period
}
