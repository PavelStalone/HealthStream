package ru.health.stream.data.vitals.usecase

import androidx.collection.FloatFloatPair
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.datetime.TimeZone
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.MeasurementGroup
import ru.health.stream.data.vitals.model.Note
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.asMeasurementGroup
import ru.health.stream.data.vitals.model.changeByPriority
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import kotlin.reflect.KClass

@Singleton
class GroupMeasurementByPeriodUseCase @Inject constructor() {

    operator fun invoke(
        period: Period,
        timeZone: TimeZone,
        measurements: List<Measurement>,
    ): Map<KClass<out Measurement>, List<MeasurementGroup>> {
        val measurementGroups: MutableMap<KClass<out Measurement>, MutableList<MeasurementGroup>> =
            mutableMapOf()

        measurements.forEach { measurement ->
            val periodRange = period.calculateRange(
                date = measurement.createdAt,
                timeZone = timeZone,
            )

            val groups = measurementGroups.getOrPut(key = measurement::class) { mutableListOf() }
            val lastGroup = groups.lastOrNull()

            val newGroup = if (lastGroup != null && lastGroup.dateRange == periodRange) {
                groups.removeAt(groups.lastIndex)
                lastGroup.mergeWithMeasurement(measurement)
            } else {
                measurement.asMeasurementGroup(measurementDateRange = periodRange)
            }

            groups.add(newGroup)
        }

        return measurementGroups
    }

    private fun MeasurementGroup.mergeWithMeasurement(measurement: Measurement): MeasurementGroup {
        val mNote = measurement[Note]?.description
        val mEstimation = measurement[Estimation]

        val newNote = note.merge(mNote)
        val newEstimation = estimation.changeByPriority(mEstimation)

        return when (this) {
            is MeasurementGroup.BloodGlucose -> copy(
                note = newNote,
                estimation = newEstimation,
                mean = mean.add((measurement as BloodGlucose).level),
                range = range.changeRange(measurement.level),
            )

            is MeasurementGroup.BloodPressure -> copy(
                note = newNote,
                estimation = newEstimation,
                systolicMean = systolicMean.add((measurement as BloodPressure).systolic.toDouble()),
                diastolicMean = diastolicMean.add(measurement.diastolic.toDouble()),
                systolicRange = systolicRange.changeRange(measurement.systolic),
                diastolicRange = diastolicRange.changeRange(measurement.diastolic),
                minBpByDifference = minBpByDifference.changeByMin(measurement.run {
                    FloatFloatPair(systolic, diastolic)
                }),
                maxBpByDifference = maxBpByDifference.changeByMax(measurement.run {
                    FloatFloatPair(systolic, diastolic)
                }),
            )

            is MeasurementGroup.BodyWeight -> copy(
                note = newNote,
                estimation = newEstimation,
                mean = mean.add((measurement as BodyWeight).weight.kg.toDouble()),
                range = range.changeRange(measurement.weight.kg),
            )

            is MeasurementGroup.HeartRate -> copy(
                note = newNote,
                estimation = newEstimation,
                mean = mean.add((measurement as HeartRate).pulse.toDouble()),
                range = range.changeRange(measurement.pulse),
            )

            is MeasurementGroup.OxygenSaturation -> copy(
                note = newNote,
                estimation = newEstimation,
                mean = mean.add((measurement as OxygenSaturation).saturation.toDouble()),
                range = range.changeRange(measurement.saturation),
            )

            is MeasurementGroup.RespirationRate -> copy(
                note = newNote,
                estimation = newEstimation,
                mean = mean.add((measurement as RespirationRate).rate),
                range = range.changeRange(measurement.rate),
            )
        }
    }

    private fun <T : Comparable<T>> ClosedRange<T>.changeRange(value: T): ClosedRange<T> {
        val newStart = if (value < start) value else start
        val newEnd = if (value > endInclusive) value else endInclusive

        return newStart..newEnd
    }

    private fun String?.merge(other: String?): String? = when {
        this == null -> other
        other == null -> this
        else -> "$this | $other"
    }

    private fun FloatFloatPair.changeByMin(other: FloatFloatPair): FloatFloatPair = when {
        first - second < other.first - other.second -> this
        else -> other
    }

    private fun FloatFloatPair.changeByMax(other: FloatFloatPair): FloatFloatPair = when {
        first - second > other.first - other.second -> this
        else -> other
    }
}
