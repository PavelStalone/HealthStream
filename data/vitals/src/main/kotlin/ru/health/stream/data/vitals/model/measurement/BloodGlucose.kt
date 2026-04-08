package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource

data class BloodGlucose(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val level: Double, // Can be value class
) : Measurement, Metadata by metadata

internal fun BloodGlucose.check() {
    require(level >= 0) { "Blood glucose level cannot be negative: $level" }
}
