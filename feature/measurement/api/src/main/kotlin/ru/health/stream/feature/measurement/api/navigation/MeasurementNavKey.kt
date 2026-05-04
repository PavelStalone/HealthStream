package ru.health.stream.feature.measurement.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.health.stream.core.common.KClassSerializer
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

@Serializable
data class MeasurementNavKey(
    @Serializable(with = KClassSerializer::class)
    val measurementType: KClass<out Measurement>,
) : NavKey

@Serializable
data class AddMeasurementNavKey(
    @Serializable(with = KClassSerializer::class)
    val measurementType: KClass<out Measurement> = HeartRate::class,
) : NavKey
