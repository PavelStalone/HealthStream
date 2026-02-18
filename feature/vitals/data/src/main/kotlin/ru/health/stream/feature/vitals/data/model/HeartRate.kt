package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

sealed interface HeartRate : HealthMeasurement {

    val pulse: Int

    data class Simple(
        override val id: String = Uuid.random().toString(),
        override val createdAt: Instant,
        override val pulse: Int,
    ) : HeartRate {

        init {
            Uuid.parse(id) // Check uuid format
        }
    }

    data class WithResource(
        private val simple: Simple,
        override val resource: Resource,
    ) : HeartRate by simple, HealthMeasurement.WithResource
}
