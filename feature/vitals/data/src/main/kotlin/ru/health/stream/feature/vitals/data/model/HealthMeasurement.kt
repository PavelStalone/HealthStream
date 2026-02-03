package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.mapper.asHeartRate
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

fun SimpleHealthMeasurement.addResource(resource: Resource): HealthMeasurement =
    when (val measurement = this) {
        is SimpleHealthMeasurement.SimpleHeartRate -> measurement.asHeartRate(resource = resource)

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
