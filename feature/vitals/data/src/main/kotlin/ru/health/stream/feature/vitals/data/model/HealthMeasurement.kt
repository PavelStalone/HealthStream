package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.mapper.asHeartRate

/**
 * Measurements with main information
 */
sealed interface HealthMeasurement {

    val id: String
    val createdAt: Instant

    sealed interface WithResource : HealthMeasurement {

        val resource: Resource
    }
}

fun HealthMeasurement.addResource(resource: Resource): HealthMeasurement.WithResource =
    when (val measurement = this) {
        is HeartRate -> measurement.asHeartRate(resource = resource)

        // Needed to skip unnecessary branches with already filled data (HealthMeasurement.HeartRate, etc.)
        else -> error("Measurements already have a resource")
    }
