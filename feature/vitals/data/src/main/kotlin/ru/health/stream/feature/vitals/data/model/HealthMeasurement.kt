package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface HealthMeasurement {

    val createdAt: Instant
    val resource: Resource
}
