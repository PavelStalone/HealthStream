package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
import kotlin.uuid.Uuid

data class HeartRate(
    override val id: String = Uuid.random().toString(),
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val pulse: Int,
) : HealthMeasurement, Metadata by metadata {
    
    init {
        Uuid.parse(id) // Check uuid format
        require(pulse >= 0) { "Heart rate cannot be negative: $pulse" }
    }
}
