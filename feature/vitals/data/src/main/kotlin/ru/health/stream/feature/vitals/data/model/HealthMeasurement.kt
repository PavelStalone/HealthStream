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
