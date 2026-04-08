package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource
import kotlin.uuid.Uuid

data class HeartRate(
    override val id: String = Uuid.random().toString(),
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val pulse: Int,
) : Measurement, Metadata by metadata

internal fun HeartRate.check() {
    require(pulse >= 0) { "Heart rate cannot be negative: $pulse" }
}
