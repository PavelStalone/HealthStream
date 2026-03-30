package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource

/**
 * Measurements with main information
 */
sealed interface HealthMeasurement : Metadata {

    val id: String
    val createdAt: Instant
    val resource: Resource
    val metadata: Metadata
}

fun HealthMeasurement.copy(metadata: Metadata) = when (this) {
    is BloodGlucose -> copy(metadata = metadata)
    is BloodPressure -> copy(metadata = metadata)
    is BodyWeight -> copy(metadata = metadata)
    is HeartRate -> copy(metadata = metadata)
    is OxygenSaturation -> copy(metadata = metadata)
    is RespirationRate -> copy(metadata = metadata)
    else -> this
}
