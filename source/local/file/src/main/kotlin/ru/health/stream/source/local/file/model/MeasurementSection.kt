package ru.health.stream.source.local.file.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.data.vitals.model.MeasurementGroup
import kotlin.time.Duration.Companion.days

internal class MeasurementSection(
    val unit: String,
    val typeName: String,
    val timeZone: TimeZone,
    val measurementGroup: MeasurementGroup,
    val reportEstimation: ReportEstimation?,
) {

    val date: String

    init {
        with(measurementGroup) {
            val duration = dateRange.endInclusive - dateRange.endInclusive

            val localStart = dateRange.start.toLocalDateTime(timeZone)
            val localEnd = dateRange.endInclusive.toLocalDateTime(timeZone)

            date = when {
                duration < 1.days -> {
                    buildString {
                        append(localStart.format(dateFormatter))
                        append(" ")
                        append(localStart.format(timeFormatter))
                        append(" - ")
                        append(localEnd.format(timeFormatter))
                    }
                }

                else -> {
                    buildString {
                        append(localStart.format(dateFormatter))
                        append(" - ")
                        append(localEnd.format(dateFormatter))
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return "MeasurementSection(typeName='$typeName', measurementGroup=$measurementGroup, date='$date')"
    }
}

private val dateFormatter = LocalDateTime.Format {
    dayOfMonth(Padding.NONE)
    char(value = '.')
    monthNumber()
    char(value = '.')
    year()
}
private val timeFormatter = LocalDateTime.Format {
    hour()
    char(value = ':')
    minute()
}

internal fun MeasurementGroup.asMeasurementSection(
    timeZone: TimeZone,
): MeasurementSection {
    val reportEstimation = estimation?.level?.asReportEstimation()

    val (typeName, unit) = when (this) {
        is MeasurementGroup.BodyWeight -> "Вес тела" to "кг"
        is MeasurementGroup.OxygenSaturation -> "Сатурация (SpO₂)" to "%"
        is MeasurementGroup.BloodGlucose -> "Глюкоза в крови" to "ммоль/л"
        is MeasurementGroup.RespirationRate -> "Частота дыхания" to "вдох/мин"
        is MeasurementGroup.HeartRate -> "Частота сердечных сокращений" to "уд/мин"
        is MeasurementGroup.BloodPressure -> "Артериальное давление" to "мм рт.ст."
    }

    return MeasurementSection(
        unit = unit,
        typeName = typeName,
        timeZone = timeZone,
        measurementGroup = this,
        reportEstimation = reportEstimation,
    )
}
