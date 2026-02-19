package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface BloodGlucose : HealthMeasurement {

    val level: Double // Can be value class

    data class Simple(
        override val id: String,
        override val level: Double,
        override val createdAt: Instant,
    ) : BloodGlucose

    data class WithResource(
        override val resource: Resource,
        private val bloodGlucose: BloodGlucose,
    ) : BloodGlucose by bloodGlucose, HealthMeasurement.WithResource
}
