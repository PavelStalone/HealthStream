package ru.health.stream.feature.vitals.data.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import kotlin.reflect.KClass

@Serializable
data object MainVitalsScreen : NavKey

@Serializable
data class MeasurementScreen(
    val measurementType: KClass<out HealthMeasurement>,
) : NavKey
