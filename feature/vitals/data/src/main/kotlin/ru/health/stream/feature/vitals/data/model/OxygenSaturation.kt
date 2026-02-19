package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface OxygenSaturation : HealthMeasurement {

    val saturation: Float

    data class Simple(
        override val id: String,
        override val saturation: Float,
        override val createdAt: Instant,
    ) : OxygenSaturation

    data class WithResource(
        override val resource: Resource,
        private val oxygenSaturation: OxygenSaturation,
    ) : OxygenSaturation by oxygenSaturation, HealthMeasurement.WithResource
}

fun OxygenSaturation.Simple.addResource(resource: Resource): OxygenSaturation.WithResource =
    OxygenSaturation.WithResource(
        resource = resource,
        oxygenSaturation = this,
    )
