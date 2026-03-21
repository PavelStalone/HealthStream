package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
import kotlin.uuid.Uuid

data class BloodGlucose(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val level: Double, // Can be value class
) : HealthMeasurement, Metadata by metadata {

    init {
        Uuid.parse(id) // Check uuid format
        require(level >= 0) { "Blood glucose level cannot be negative: $level" }
    }
}
