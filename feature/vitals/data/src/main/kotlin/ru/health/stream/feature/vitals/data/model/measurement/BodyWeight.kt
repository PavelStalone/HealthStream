package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.Weight
import kotlin.uuid.Uuid

data class BodyWeight(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val weight: Weight,
) : HealthMeasurement, Metadata by metadata {

    init {
        Uuid.parse(id) // Check uuid format
        require(weight.kg > 0) { "Вес должен быть положительным" }
    }
}
