package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

sealed interface HeartRate : HealthMeasurement {

    val pulse: Int

    data class Simple(
        override val id: String = Uuid.random().toString(),
        override val pulse: Int,
        override val createdAt: Instant,
    ) : HeartRate {

        init {
            Uuid.parse(id) // Check uuid format
        }
    }

    data class WithResource(
        private val heartRate: Simple,
        override val resource: Resource,
    ) : HeartRate by heartRate, HealthMeasurement.WithResource
}

fun HeartRate.Simple.addResource(resource: Resource): HeartRate.WithResource =
    HeartRate.WithResource(
        heartRate = this,
        resource = resource,
    )
