package ru.health.stream.data.vitals.model

import androidx.collection.FloatFloatPair
import kotlinx.datetime.Instant
import ru.health.stream.core.common.model.Mean
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.DiastolicPressure
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.model.measurement.SystolicPressure

sealed interface MeasurementGroup {

    val note: String?
    val aggregateValue: String
    val estimation: Estimation?
    val dateRange: ClosedRange<Instant>

    data class BodyWeight(
        val mean: Mean,
        val range: ClosedRange<Float>,
        override val note: String?,
        override val estimation: Estimation?,
        override val dateRange: ClosedRange<Instant>,
    ) : MeasurementGroup {

        override val aggregateValue: String
            get() = "${range.start} - ${range.endInclusive}"
    }

    data class BloodGlucose(
        val mean: Mean,
        val range: ClosedRange<Double>,
        override val note: String?,
        override val estimation: Estimation?,
        override val dateRange: ClosedRange<Instant>,
    ) : MeasurementGroup {

        override val aggregateValue: String
            get() = "${range.start} - ${range.endInclusive}"
    }

    data class HeartRate(
        val mean: Mean,
        val range: ClosedRange<Int>,
        override val note: String?,
        override val estimation: Estimation?,
        override val dateRange: ClosedRange<Instant>,
    ) : MeasurementGroup {

        override val aggregateValue: String
            get() = "${range.start} - ${range.endInclusive}"
    }

    data class OxygenSaturation(
        val mean: Mean,
        val range: ClosedRange<Float>,
        override val note: String?,
        override val estimation: Estimation?,
        override val dateRange: ClosedRange<Instant>,
    ) : MeasurementGroup {

        override val aggregateValue: String
            get() = "${range.start} - ${range.endInclusive}"
    }

    data class RespirationRate(
        val mean: Mean,
        val range: ClosedRange<Double>,
        override val note: String?,
        override val estimation: Estimation?,
        override val dateRange: ClosedRange<Instant>,
    ) : MeasurementGroup {

        override val aggregateValue: String
            get() = "${range.start} - ${range.endInclusive}"
    }

    data class BloodPressure(
        val systolicMean: Mean,
        val diastolicMean: Mean,
        val systolicRange: ClosedRange<Float>,
        val diastolicRange: ClosedRange<Float>,
        val minBpByDifference: FloatFloatPair,
        val maxBpByDifference: FloatFloatPair,
        override val note: String?,
        override val estimation: Estimation?,
        override val dateRange: ClosedRange<Instant>,
    ) : MeasurementGroup {

        override val aggregateValue: String
            get() = buildString {
                append("${systolicRange.start.toInt()} - ${systolicRange.endInclusive.toInt()}/${diastolicRange.start.toInt()} - ${diastolicRange.endInclusive.toInt()}")
                append("\n${minBpByDifference.first.toInt()}/${minBpByDifference.second.toInt()} - ${maxBpByDifference.first.toInt()}/${maxBpByDifference.second.toInt()}")
            }
    }
}

fun Measurement.asMeasurementGroup(
    measurementDateRange: ClosedRange<Instant>,
): MeasurementGroup {
    val mNote = metadata[Note]?.description
    val mEstimation = metadata[Estimation]

    return when (this) {
        is HeartRate -> MeasurementGroup.HeartRate(
            note = mNote,
            estimation = mEstimation,
            range = pulse..pulse,
            mean = Mean(pulse.toDouble()),
            dateRange = measurementDateRange,
        )

        is BodyWeight -> MeasurementGroup.BodyWeight(
            note = mNote,
            estimation = mEstimation,
            range = weight.kg..weight.kg,
            dateRange = measurementDateRange,
            mean = Mean(weight.kg.toDouble()),
        )

        is BloodGlucose -> MeasurementGroup.BloodGlucose(
            note = mNote,
            mean = Mean(level),
            range = level..level,
            estimation = mEstimation,
            dateRange = measurementDateRange,
        )

        is BloodPressure -> MeasurementGroup.BloodPressure(
            note = mNote,
            estimation = mEstimation,
            dateRange = measurementDateRange,
            systolicRange = systolic..systolic,
            diastolicRange = diastolic..diastolic,
            systolicMean = Mean(systolic.toDouble()),
            diastolicMean = Mean(diastolic.toDouble()),
            minBpByDifference = FloatFloatPair(systolic, diastolic),
            maxBpByDifference = FloatFloatPair(systolic, diastolic),
        )

        is RespirationRate -> MeasurementGroup.RespirationRate(
            note = mNote,
            mean = Mean(rate),
            range = rate..rate,
            estimation = mEstimation,
            dateRange = measurementDateRange,
        )

        is OxygenSaturation -> MeasurementGroup.OxygenSaturation(
            note = mNote,
            estimation = mEstimation,
            range = saturation..saturation,
            dateRange = measurementDateRange,
            mean = Mean(saturation.toDouble()),
        )

        is SystolicPressure -> TODO()
        is DiastolicPressure -> TODO()
    }
}
