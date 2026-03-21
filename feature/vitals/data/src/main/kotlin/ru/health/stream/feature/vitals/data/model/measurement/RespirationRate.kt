package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
import kotlin.uuid.Uuid

data class RespirationRate(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val rate: Double,
) : HealthMeasurement, Metadata by metadata {

    init {
        Uuid.parse(id) // Check uuid format
        require(rate >= 0) { "Respiration rate cannot be negative: $rate" }
    }
}
