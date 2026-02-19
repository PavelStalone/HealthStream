package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

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
        is HealthMeasurement.WithResource -> this
        is HeartRate.Simple -> measurement.addResource(resource = resource)

        else -> error("This measurements can`t be have resource")
    }
