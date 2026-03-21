package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource

/**
 * Measurements with main information
 */
sealed interface HealthMeasurement : Metadata {

    val id: String
    val createdAt: Instant
    val resource: Resource
    val metadata: Metadata
}
