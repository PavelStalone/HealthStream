package ru.health.stream.feature.vitals.domain

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.drawable.CubicArea
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.model.ChartPosition
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
    val drawables: List<Drawable>,
    val xRange: ClosedFloatingPointRange<Float>,
    val yRange: ClosedFloatingPointRange<Float>,
) {

    companion object Factory {

        fun create(
            period: Period,
            dateNow: Instant,
            timeZone: TimeZone,
            measurements: List<HealthMeasurement>,
            primaryColor: Color = Color(0xFF0061A4), // Need to change it dynamically?
        ): DrawableData {
            val pointsMap = mutableMapOf<KClass<*>, MutableList<ChartPosition.Point>>()
            val positionTransformer = DatePositionTransformer(
                period = period,
                dateNow = dateNow,
                timeZone = timeZone,
            )

            fun addPoint(clazz: KClass<*>, x: Float, y: Float) {
                pointsMap.getOrPut(key = clazz) { mutableListOf() }
                    .add(ChartPosition.Point(x = x, y = y, z = 0f))
            }

            measurements.forEach { measurement ->
                val x = positionTransformer.transform(date = measurement.createdAt)
                val type = measurement::class

                when (measurement) {
                    is BloodPressure -> {
                        addPoint(SystolicPressure::class, x, measurement.systolic)
                        addPoint(DiastolicPressure::class, x, measurement.diastolic)
                    }

                    is BodyWeight -> addPoint(type, x, measurement.weight.kg)
                    is HeartRate -> addPoint(type, x, measurement.pulse.toFloat())
                    is SystolicPressure -> addPoint(type, x, measurement.systolic)
                    is DiastolicPressure -> addPoint(type, x, measurement.diastolic)
                    is OxygenSaturation -> addPoint(type, x, measurement.saturation)
                    is BloodGlucose -> addPoint(type, x, measurement.level.toFloat())
                    is RespirationRate -> addPoint(type, x, measurement.rate.toFloat())
                }
            }

            val yRange = pointsMap.values.asSequence()
                .flatten()
                .let { points ->
                    val min = points.minOfOrNull { it.y } ?: 0f
                    val max = points.maxOfOrNull { it.y } ?: 1f
                    min..max
                }

            val charts = pointsMap.values.flatMap { positions ->
                val sorted = positions.sortedBy { position -> position.x }
                if (sorted.size >= 2) {
                    listOf(
                        CubicArea(
                            points = sorted,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    primaryColor.copy(alpha = 0.0f)
                                )
                            )
                        ),
                        CubicLine(
                            points = sorted,
                            style = Stroke(width = 8f),
                            color = primaryColor,
                        )
                    )
                } else emptyList()
            }

            return DrawableData(
                xRange = 0f..1f,
                yRange = yRange,
                drawables = charts,
            )
        }
    }
}
