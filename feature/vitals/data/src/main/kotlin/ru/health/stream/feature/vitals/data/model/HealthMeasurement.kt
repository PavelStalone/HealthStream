package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface HealthMeasurement {

    val createdAt: Instant
    val resource: Resource

    data class HeartRate(
        override val createdAt: Instant,
        override val resource: Resource,
        val pulse: Int,
    ): HealthMeasurement
}
