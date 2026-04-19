package ru.health.stream.source.local.file.model

import androidx.collection.FloatFloatPair
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

sealed class MeasurementSection(
    val typeName: String,
    val valueText: String,
    open val note: String?,
    open val timeZone: TimeZone,
    open val dateRange: ClosedRange<Instant>,
    open val reportEstimation: ReportEstimation?,
) {

    val date: String by lazy {
        val duration = dateRange.endInclusive - dateRange.endInclusive

        val localStart = dateRange.start.toLocalDateTime(timeZone)
        val localEnd = dateRange.endInclusive.toLocalDateTime(timeZone)

        when {
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

    data class BodyWeight(
        val weightMean: Mean,
        val weightRange: ClosedRange<Float>,
        override val note: String?,
        override val timeZone: TimeZone,
        override val dateRange: ClosedRange<Instant>,
        override val reportEstimation: ReportEstimation?,
    ) : MeasurementSection(
        note = note,
        timeZone = timeZone,
        dateRange = dateRange,
        typeName = "Вес тела",
        reportEstimation = reportEstimation,
        valueText = "${weightRange.start} - ${weightRange.endInclusive} кг",
    )

    data class BloodGlucose(
        val levelMean: Mean,
        val levelRange: ClosedRange<Double>,
        override val note: String?,
        override val timeZone: TimeZone,
        override val dateRange: ClosedRange<Instant>,
        override val reportEstimation: ReportEstimation?,
    ) : MeasurementSection(
        note = note,
        timeZone = timeZone,
        dateRange = dateRange,
        typeName = "Глюкоза в крови",
        reportEstimation = reportEstimation,
        valueText = "${levelRange.start} - ${levelRange.endInclusive} ммоль/л",
    )

    data class HeartRate(
        val pulseMean: Mean,
        val pulseRange: ClosedRange<Int>,
        override val note: String?,
        override val timeZone: TimeZone,
        override val dateRange: ClosedRange<Instant>,
        override val reportEstimation: ReportEstimation?,
    ) : MeasurementSection(
        note = note,
        timeZone = timeZone,
        dateRange = dateRange,
        reportEstimation = reportEstimation,
        typeName = "Частота сердечных сокращений",
        valueText = "${pulseRange.start} - ${pulseRange.endInclusive} уд/мин",
    )

    data class OxygenSaturation(
        val saturationMean: Mean,
        val saturationRange: ClosedRange<Float>,
        override val note: String?,
        override val timeZone: TimeZone,
        override val dateRange: ClosedRange<Instant>,
        override val reportEstimation: ReportEstimation?,
    ) : MeasurementSection(
        note = note,
        timeZone = timeZone,
        dateRange = dateRange,
        typeName = "Сатурация (SpO2)",
        reportEstimation = reportEstimation,
        valueText = "${saturationRange.start} - ${saturationRange.endInclusive} %",
    )

    data class RespirationRate(
        val rateMean: Mean,
        val rateRange: ClosedRange<Double>,
        override val note: String?,
        override val timeZone: TimeZone,
        override val dateRange: ClosedRange<Instant>,
        override val reportEstimation: ReportEstimation?,
    ) : MeasurementSection(
        note = note,
        timeZone = timeZone,
        dateRange = dateRange,
        typeName = "Сатурация (SpO2)",
        reportEstimation = reportEstimation,
        valueText = "${rateRange.start} - ${rateRange.endInclusive} вдох/мин",
    )

    data class BloodPressure(
        val systolicMean: Mean,
        val diastolicMean: Mean,
        val systolicRange: ClosedRange<Float>,
        val diastolicRange: ClosedRange<Float>,
        val minBpByDifference: FloatFloatPair,
        val maxBpByDifference: FloatFloatPair,
        override val note: String?,
        override val timeZone: TimeZone,
        override val dateRange: ClosedRange<Instant>,
        override val reportEstimation: ReportEstimation?,
    ) : MeasurementSection(
        note = note,
        timeZone = timeZone,
        dateRange = dateRange,
        typeName = "Артериальное давление",
        reportEstimation = reportEstimation,
        valueText = buildString {
            append("${systolicRange.start} - ${systolicRange.endInclusive}/${diastolicRange.start} - ${diastolicRange.endInclusive}")
            appendLine(" мм рт.ст.")
            append("${minBpByDifference.first}/${minBpByDifference.second} - ${maxBpByDifference.first}/${maxBpByDifference.second}")
            append(" мм рт.ст.")
        },
    )
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

data class Mean(
    val mean: Double,
    val count: Int = 1
) {

    fun add(value: Double): Mean = Mean(
        mean = mean + (value - mean) / (count + 1),
        count = count + 1
    )
}
