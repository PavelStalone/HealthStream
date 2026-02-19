package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface BodyWeight : HealthMeasurement {

    val weight: Float // Can be value class

    data class Simple(
        override val id: String,
        override val weight: Float,
        override val createdAt: Instant
    ) : BodyWeight

    data class WithResource(
        override val resource: Resource,
        private val bodyWeight: BodyWeight,
    ) : BodyWeight by bodyWeight, HealthMeasurement.WithResource
}
