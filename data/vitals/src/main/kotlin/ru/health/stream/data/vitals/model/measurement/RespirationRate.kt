package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource

data class RespirationRate(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val rate: Double,
) : Measurement, Metadata by metadata

internal fun RespirationRate.check() {
    require(rate >= 0) { "Respiration rate cannot be negative: $rate" }
}
