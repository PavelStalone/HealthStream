package ru.health.stream.feature.measurement.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

@Serializable
data class MeasurementNavKey(
    val measurementType: KClass<out Measurement>,
) : NavKey

@Serializable
data class AddMeasurementNavKey(
    val measurementType: KClass<out Measurement> = HeartRate::class,
) : NavKey
