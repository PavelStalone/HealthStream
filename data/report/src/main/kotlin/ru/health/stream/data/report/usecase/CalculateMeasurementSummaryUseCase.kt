package ru.health.stream.data.report.usecase

import androidx.collection.FloatFloatPair
import jakarta.inject.Inject
import jakarta.inject.Singleton
import ru.health.stream.data.report.model.MeasurementSummary
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.MeasurementGroup
import ru.health.stream.data.vitals.model.asMeasurementGroup
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import kotlin.reflect.KClass

@Singleton
class CalculateMeasurementSummaryUseCase @Inject constructor() {

    operator fun invoke(
        measurements: List<Measurement>,
    ): Map<KClass<out Measurement>, MeasurementSummary> {
        val measurementSummary: MutableMap<KClass<out Measurement>, MeasurementSummary> =
            mutableMapOf()

        measurements.forEach { measurement ->
            val summary = measurementSummary[measurement::class]

            val newSummary = with(measurement) {
                summary?.mergeWithMeasurement(measurement)
                    ?: MeasurementSummary(
                        counts = 1,
                        group = asMeasurementGroup(measurementDateRange = createdAt..createdAt),
                        estimationsCount = metadata[Estimation]?.level?.let { estimation ->
                            mapOf(estimation to 1)
                        } ?: emptyMap()
                    )
            }

            measurementSummary[measurement::class] = newSummary
        }

        return measurementSummary
    }

    private fun MeasurementSummary.mergeWithMeasurement(
        measurement: Measurement,
    ): MeasurementSummary {
        val estimation = measurement[Estimation]?.level
        val measurementDateRange = measurement.createdAt..measurement.createdAt

        return MeasurementSummary(
            counts = counts + 1,
            group = with(group) {
                when (this) {
                    is MeasurementGroup.BloodGlucose -> copy(
                        dateRange = dateRange.changeRange(measurementDateRange),
                        range = range.changeRange((measurement as BloodGlucose).level),
                    )

                    is MeasurementGroup.BloodPressure -> copy(
                        dateRange = dateRange.changeRange(measurementDateRange),
                        systolicRange = systolicRange.changeRange((measurement as BloodPressure).systolic),
                        diastolicRange = diastolicRange.changeRange(measurement.diastolic),
                        minBpByDifference = minBpByDifference.changeByMin(measurement.run {
                            FloatFloatPair(systolic, diastolic)
                        }),
                        maxBpByDifference = maxBpByDifference.changeByMax(measurement.run {
                            FloatFloatPair(systolic, diastolic)
                        }),
                    )

                    is MeasurementGroup.BodyWeight -> copy(
                        dateRange = dateRange.changeRange(measurementDateRange),
                        range = range.changeRange((measurement as BodyWeight).weight.kg),
                    )

                    is MeasurementGroup.HeartRate -> copy(
                        dateRange = dateRange.changeRange(measurementDateRange),
                        range = range.changeRange((measurement as HeartRate).pulse),
                    )

                    is MeasurementGroup.OxygenSaturation -> copy(
                        dateRange = dateRange.changeRange(measurementDateRange),
                        range = range.changeRange((measurement as OxygenSaturation).saturation),
                    )

                    is MeasurementGroup.RespirationRate -> copy(
                        dateRange = dateRange.changeRange(measurementDateRange),
                        range = range.changeRange((measurement as RespirationRate).rate),
                    )
                }
            },
            estimationsCount = estimation?.let {
                estimationsCount + (estimation to (estimationsCount[estimation] ?: 0) + 1)
            } ?: estimationsCount
        )
    }

    private fun <T : Comparable<T>> ClosedRange<T>.changeRange(value: T): ClosedRange<T> {
        val newStart = if (value < start) value else start
        val newEnd = if (value > endInclusive) value else endInclusive

        return newStart..newEnd
    }

    private fun <T : Comparable<T>> ClosedRange<T>.changeRange(
        value: ClosedRange<T>
    ): ClosedRange<T> = changeRange(value.start).changeRange(value.endInclusive)

    private fun FloatFloatPair.changeByMin(other: FloatFloatPair): FloatFloatPair = when {
        first - second < other.first - other.second -> this
        else -> other
    }

    private fun FloatFloatPair.changeByMax(other: FloatFloatPair): FloatFloatPair = when {
        first - second > other.first - other.second -> this
        else -> other
    }
}
