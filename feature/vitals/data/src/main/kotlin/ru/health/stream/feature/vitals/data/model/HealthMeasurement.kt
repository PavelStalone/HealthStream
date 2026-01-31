package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Measurements with main information
 */
sealed interface SimpleHealthMeasurement {

    val id: String
    val createdAt: Instant

    interface HeartRateData : SimpleHealthMeasurement {
        val pulse: Int
    }

    @OptIn(ExperimentalUuidApi::class)
    data class SimpleHeartRate(
        override val id: String = Uuid.random().toString(),
        override val createdAt: Instant,
        override val pulse: Int,
    ) : HeartRateData {

        init {
            Uuid.parse(id) // Check uuid format
        }
    }
}

fun SimpleHealthMeasurement.addResource(resource: Resource): HealthMeasurement = when (this) {
    // TODO: Change mapping to code generation after the release of a new version - shoplikpavel 2026-01-31
    is SimpleHealthMeasurement.HeartRateData -> HealthMeasurement.HeartRate(
        id = id,
        pulse = pulse,
        resource = resource,
        createdAt = createdAt,
    )

    // Needed to skip unnecessary branches with already filled data (HealthMeasurement.HeartRate, etc.)
    else -> error("Measurements already have a resource")
}

/**
 * Measurements with general information
 */
sealed interface HealthMeasurement : SimpleHealthMeasurement {

    val resource: Resource

    @OptIn(ExperimentalUuidApi::class)
    data class HeartRate(
        override val id: String = Uuid.random().toString(),
        override val createdAt: Instant,
        override val resource: Resource,
        override val pulse: Int,
    ) : HealthMeasurement, SimpleHealthMeasurement.HeartRateData {

        init {
            Uuid.parse(id) // Check uuid format
        }
    }
}
