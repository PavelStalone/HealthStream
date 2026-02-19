package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface RespirationRate : HealthMeasurement {

    val rate: Double

    data class Simple(
        override val id: String,
        override val rate: Double,
        override val createdAt: Instant,
    ) : RespirationRate

    data class WithResource(
        override val resource: Resource,
        private val respirationRate: RespirationRate,
    ) : RespirationRate by respirationRate, HealthMeasurement.WithResource
}
