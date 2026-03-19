package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
import kotlin.uuid.Uuid

data class OxygenSaturation(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val saturation: Float,
) : HealthMeasurement, Metadata by metadata {

    init {
        Uuid.parse(id) // Check uuid format
        require(saturation in 0f..100f) { "Oxygen saturation must be between 0 and 100: $saturation" }
    }
}
