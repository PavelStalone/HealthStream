package ru.health.stream.feature.chart.model

import androidx.collection.FloatFloatPair
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.health.stream.data.vitals.model.MeasurementGroup
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.usecase.DateTransformerUseCase
import ru.health.stream.data.vitals.usecase.GroupMeasurementByPeriodUseCase
import ru.health.stream.feature.chart.model.ChartPosition
import kotlin.math.max
import kotlin.math.min

@Immutable
data class DrawableData(
    val xRange: ClosedFloatingPointRange<Float>,
    val yRange: ClosedFloatingPointRange<Float>,
    val scatterPositions: List<List<ChartPosition>>,
    val pointPositions: List<List<ChartPosition.Point>>,
) {

    companion object Factory {

        fun create(
            period: Period,
            timeZone: TimeZone,
            measurements: List<Measurement>,
            dateRange: ClosedRange<Instant>,
            groupMeasurementByPeriodUseCase: GroupMeasurementByPeriodUseCase,
        ): DrawableData {
            if (measurements.isEmpty()) {
                return DrawableData(
                    scatterPositions = emptyList(),
                    pointPositions = emptyList(),
                    xRange = 0f..1f,
                    yRange = 0f..0f,
                )
            }

            val dateTransformerUseCase = DateTransformerUseCase(
                period = period,
                timeZone = timeZone,
                dateRange = dateRange,
            )
            val groups = groupMeasurementByPeriodUseCase(
                period = period,
                timeZone = timeZone,
                measurements = measurements,
            )

            val scatterPositions = mutableListOf<List<ChartPosition>>()
            val pointPositions = mutableListOf<List<ChartPosition.Point>>()

            var yMin = Float.MAX_VALUE
            var yMax = Float.MIN_VALUE

            groups.values.forEach { groups ->
                val positionMap: MutableMap<Int, MutableList<ChartPosition>> = mutableMapOf()

                groups.forEach { group ->
                    val x = dateTransformerUseCase(
                        date = group.dateRange.start.plus((group.dateRange.endInclusive - group.dateRange.start) / 2)
                    )

                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    when (group) {
                        is MeasurementGroup.BloodPressure -> {
                            val scatterPositionsD = positionMap.getOrPut(2) { mutableListOf() }
                            val meanPositionsD = positionMap.getOrPut(3) { mutableListOf() }

                            scatterPositions.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.systolicRange.start,
                                        second = group.systolicRange.endInclusive,
                                    )
                                )
                            )
                            scatterPositionsD.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.diastolicRange.start,
                                        second = group.diastolicRange.endInclusive,
                                    )
                                )
                            )

                            meanPositions.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.systolicMean.value.toFloat(),
                                )
                            )
                            meanPositionsD.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.diastolicMean.value.toFloat(),
                                )
                            )

                            yMin = min(group.systolicRange.start, yMin)
                            yMin = min(group.diastolicRange.start, yMin)
                            yMax = max(group.systolicRange.endInclusive, yMax)
                            yMax = max(group.diastolicRange.endInclusive, yMax)
                        }

                        is MeasurementGroup.BloodGlucose -> {
                            scatterPositions.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.range.start.toFloat(),
                                        second = group.range.endInclusive.toFloat(),
                                    )
                                )
                            )

                            meanPositions.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.mean.value.toFloat(),
                                )
                            )

                            yMin = min(group.range.start.toFloat(), yMin)
                            yMax = max(group.range.endInclusive.toFloat(), yMax)
                        }

                        is MeasurementGroup.BodyWeight -> {
                            scatterPositions.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.range.start,
                                        second = group.range.endInclusive,
                                    )
                                )
                            )

                            meanPositions.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.mean.value.toFloat(),
                                )
                            )

                            yMin = min(group.range.start, yMin)
                            yMax = max(group.range.endInclusive, yMax)
                        }

                        is MeasurementGroup.HeartRate -> {
                            scatterPositions.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.range.start.toFloat(),
                                        second = group.range.endInclusive.toFloat(),
                                    )
                                )
                            )

                            meanPositions.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.mean.value.toFloat(),
                                )
                            )

                            yMin = min(group.range.start.toFloat(), yMin)
                            yMax = max(group.range.endInclusive.toFloat(), yMax)
                        }

                        is MeasurementGroup.OxygenSaturation -> {
                            scatterPositions.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.range.start,
                                        second = group.range.endInclusive,
                                    )
                                )
                            )

                            meanPositions.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.mean.value.toFloat(),
                                )
                            )

                            yMin = min(group.range.start, yMin)
                            yMax = max(group.range.endInclusive, yMax)
                        }

                        is MeasurementGroup.RespirationRate -> {
                            scatterPositions.add(
                                ChartPosition.Range.Vertical(
                                    x = x,
                                    y = FloatFloatPair(
                                        first = group.range.start.toFloat(),
                                        second = group.range.endInclusive.toFloat(),
                                    )
                                )
                            )

                            meanPositions.add(
                                ChartPosition.Point(
                                    x = x,
                                    y = group.mean.value.toFloat(),
                                )
                            )

                            yMin = min(group.range.start.toFloat(), yMin)
                            yMax = max(group.range.endInclusive.toFloat(), yMax)
                        }
                    }
                }

                positionMap.forEach { (i, positions) ->
                    if (i % 2 == 0) {
                        scatterPositions.add(positions)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val points = positions as List<ChartPosition.Point>

                        pointPositions.add(points)
                    }
                }
            }

            return DrawableData(
                scatterPositions = scatterPositions,
                pointPositions = pointPositions,
                xRange = 0f..1f,
                yRange = yMin..yMax,
            )
        }
    }
}
