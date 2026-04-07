package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.Weight

data class BodyWeight(
    override val id: String,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
    val weight: Weight,
) : Measurement, Metadata by metadata

internal fun BodyWeight.check() {
    require(weight.kg > 0) { "Вес должен быть положительным" }
}
