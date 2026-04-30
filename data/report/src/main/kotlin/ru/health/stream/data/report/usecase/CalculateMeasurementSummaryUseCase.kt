package ru.health.stream.data.report.usecase

import androidx.collection.FloatFloatPair
import jakarta.inject.Inject
import jakarta.inject.Singleton
import ru.health.stream.data.report.model.MeasurementSummary
import ru.health.stream.data.vitals.model.MeasurementGroup

@Singleton
class CalculateMeasurementSummaryUseCase @Inject constructor() {

    operator fun invoke(
        measurementGroups: List<MeasurementGroup>,
    ): MeasurementSummary? {
        var measurementSummary: MeasurementSummary? = null

        measurementGroups.forEach { measurementGroup ->
            val estimation = measurementGroup.estimation?.level

            val newSummary = measurementSummary
                ?.mergeWithMeasurementGroup(measurementGroup = measurementGroup)
                ?: MeasurementSummary(
                    counts = 1,
                    group = measurementGroup,
                    estimationsCount = estimation?.let { estimation -> mapOf(estimation to 1) }
                        ?: emptyMap()
                )

            measurementSummary = newSummary
        }

        return measurementSummary
    }


    private fun MeasurementSummary.mergeWithMeasurementGroup(
        measurementGroup: MeasurementGroup,
    ): MeasurementSummary {
        val estimation = measurementGroup.estimation?.level

        return MeasurementSummary(
            counts = counts + 1,
            group = with(group) {
                when (this) {
                    is MeasurementGroup.BloodGlucose -> copy(
                        range = range.changeRange((measurementGroup as MeasurementGroup.BloodGlucose).range),
                    )

                    is MeasurementGroup.BloodPressure -> copy(
                        systolicRange = systolicRange.changeRange((measurementGroup as MeasurementGroup.BloodPressure).systolicRange),
                        diastolicRange = diastolicRange.changeRange(measurementGroup.diastolicRange),
                        minBpByDifference = minBpByDifference.changeByMin(measurementGroup.minBpByDifference),
                        maxBpByDifference = maxBpByDifference.changeByMax(measurementGroup.maxBpByDifference),
                    )

                    is MeasurementGroup.BodyWeight -> copy(
                        range = range.changeRange((measurementGroup as MeasurementGroup.BodyWeight).range),
                    )

                    is MeasurementGroup.HeartRate -> copy(
                        range = range.changeRange((measurementGroup as MeasurementGroup.HeartRate).range),
                    )

                    is MeasurementGroup.OxygenSaturation -> copy(
                        range = range.changeRange((measurementGroup as MeasurementGroup.OxygenSaturation).range),
                    )

                    is MeasurementGroup.RespirationRate -> copy(
                        range = range.changeRange((measurementGroup as MeasurementGroup.RespirationRate).range),
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
