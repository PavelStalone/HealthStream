package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource

data class OxygenSaturation(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val saturation: Float,
) : Measurement, Metadata by metadata

internal fun OxygenSaturation.check() {
    require(saturation in 0f..100f) { "Oxygen saturation must be between 0 and 100: $saturation" }
}
