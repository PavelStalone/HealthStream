package ru.health.stream.feature.vitals.domain

import androidx.collection.FloatFloatPair
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.chart.model.max
import ru.health.stream.feature.chart.model.min
import ru.health.stream.feature.vitals.data.model.Period
import ru.health.stream.feature.vitals.data.model.measurement.BloodGlucose
import ru.health.stream.feature.vitals.data.model.measurement.BloodPressure
import ru.health.stream.feature.vitals.data.model.measurement.BodyWeight
import ru.health.stream.feature.vitals.data.model.measurement.DiastolicPressure
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import ru.health.stream.feature.vitals.data.model.measurement.OxygenSaturation
import ru.health.stream.feature.vitals.data.model.measurement.RespirationRate
import ru.health.stream.feature.vitals.data.model.measurement.SystolicPressure
import kotlin.reflect.KClass

@Immutable
data class DrawableData(
    val positions: List<List<ChartPosition>>,
    val xRange: ClosedFloatingPointRange<Float>,
    val yRange: ClosedFloatingPointRange<Float>,
) {

    companion object Factory {

        suspend fun create(
            period: Period,
            dateNow: Instant,
            timeZone: TimeZone,
            coroutineScope: CoroutineScope,
            measurements: List<HealthMeasurement>,
            groupMeasurementUseCase: GroupMeasurementUseCase,
        ): DrawableData {
            if (measurements.isEmpty()) {
                return DrawableData(emptyList(), 0f..1f, 0f..0f)
            }

            val positionTransformer = DatePositionTransformer(
                period = period,
                dateNow = dateNow,
                timeZone = timeZone,
            )

            val positions = with(coroutineScope) {
                measurements.splitByType()
                    .map { (clazz, items) ->
                        async {
                            groupAndTransform(
                                clazz = clazz,
                                items = items,
                                period = period,
                                timeZone = timeZone,
                                positionTransformer = positionTransformer,
                                groupMeasurementUseCase = groupMeasurementUseCase,
                            )
                        }
                    }
                    .awaitAll()
            }

            return DrawableData(
                positions = positions,
                xRange = 0f..1f,
                yRange = positions.calculateYRange(),
            )
        }

        private fun List<HealthMeasurement>.splitByType(): Map<KClass<out HealthMeasurement>, List<HealthMeasurement>> {
            val result =
                mutableMapOf<KClass<out HealthMeasurement>, MutableList<HealthMeasurement>>()

            forEach { measurement ->
                when (measurement) {
                    is BloodPressure -> {
                        result.getOrPut(SystolicPressure::class) { mutableListOf() }
                            .add(measurement)
                        result.getOrPut(DiastolicPressure::class) { mutableListOf() }
                            .add(measurement)
                    }

                    is BodyWeight -> result.getOrPut(BodyWeight::class) { mutableListOf() }
                        .add(measurement)

                    is HeartRate -> result.getOrPut(HeartRate::class) { mutableListOf() }
                        .add(measurement)

                    is OxygenSaturation -> result.getOrPut(OxygenSaturation::class) { mutableListOf() }
                        .add(measurement)

                    is BloodGlucose -> result.getOrPut(BloodGlucose::class) { mutableListOf() }
                        .add(measurement)

                    is RespirationRate -> result.getOrPut(RespirationRate::class) { mutableListOf() }
                        .add(measurement)

                    is SystolicPressure -> result.getOrPut(SystolicPressure::class) { mutableListOf() }
                        .add(measurement)

                    is DiastolicPressure -> result.getOrPut(DiastolicPressure::class) { mutableListOf() }
                        .add(measurement)
                }
            }

            return result
        }

        private fun groupAndTransform(
            clazz: KClass<out HealthMeasurement>,
            items: List<HealthMeasurement>,
            period: Period,
            timeZone: TimeZone,
            groupMeasurementUseCase: GroupMeasurementUseCase,
            positionTransformer: DatePositionTransformer,
        ): List<ChartPosition> {
            val grouped = groupMeasurementUseCase.invoke(
                measurements = items,
                groupSelector = { measurement ->
                    calculateGroupStart(measurement.createdAt, period, timeZone)
                },
            )

            return grouped.map { (instant, result) ->
                val x = positionTransformer.transform(instant)
                val yMax = getValueByType(clazz, result.max)

                if (result.max == result.min) {
                    ChartPosition.Point(x = x, y = yMax)
                } else {
                    val yMin = getValueByType(clazz, result.min)
                    ChartPosition.Range.Vertical(x = x, y = FloatFloatPair(yMin, yMax))
                }
            }
        }

        private fun List<List<ChartPosition>>.calculateYRange(): ClosedFloatingPointRange<Float> {
            var minY = Float.POSITIVE_INFINITY
            var maxY = Float.NEGATIVE_INFINITY
            var hasData = false

            for (list in this) {
                for (position in list) {
                    hasData = true
                    val (pMin, pMax) = when (position) {
                        is ChartPosition.Point -> position.y to position.y
                        is ChartPosition.Range.Horizontal -> position.y to position.y
                        is ChartPosition.Range.Vertical -> position.y.min() to position.y.max()
                    }
                    if (pMin < minY) minY = pMin
                    if (pMax > maxY) maxY = pMax
                }
            }

            return if (hasData) minY..maxY else 0f..0f
        }

        private fun getValueByType(
            clazz: KClass<out HealthMeasurement>,
            measurement: HealthMeasurement
        ): Float = when (clazz) {
            BodyWeight::class -> (measurement as BodyWeight).weight.kg
            HeartRate::class -> (measurement as HeartRate).pulse.toFloat()
            BloodGlucose::class -> (measurement as BloodGlucose).level.toFloat()
            SystolicPressure::class -> (measurement as SystolicPressure).systolic
            DiastolicPressure::class -> (measurement as DiastolicPressure).diastolic
            OxygenSaturation::class -> (measurement as OxygenSaturation).saturation
            RespirationRate::class -> (measurement as RespirationRate).rate.toFloat()
            BloodPressure::class -> (measurement as BloodPressure).systolic
            else -> 0f
        }

        private fun calculateGroupStart(
            instant: Instant,
            period: Period,
            timeZone: TimeZone
        ): Instant {
            val dt = instant.toLocalDateTime(timeZone)

            return when (period) {
                is Period.Day -> {
                    LocalDateTime(dt.year, dt.month, dt.dayOfMonth, dt.hour, 0).toInstant(timeZone)
                }

                is Period.Week -> {
                    LocalDateTime(dt.year, dt.month, dt.dayOfMonth, 0, 0).toInstant(timeZone)
                }

                is Period.Month -> {
                    val groupStartDay = ((dt.dayOfMonth - 1) / 2) * 2 + 1
                    LocalDateTime(dt.year, dt.month, groupStartDay, 0, 0).toInstant(timeZone)
                }

                is Period.Year -> {
                    LocalDateTime(dt.year, dt.month, 1, 0, 0).toInstant(timeZone)
                }
            }
        }
    }
}
