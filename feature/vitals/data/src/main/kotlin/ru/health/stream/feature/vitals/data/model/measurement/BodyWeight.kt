package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
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
        require(weight.value > 0) { "Weight must be positive: ${weight.value}" }
    }
}

@JvmInline
value class Weight(val value: Float) : Comparable<Weight> {

    init {
        require(value >= 0) { "Weight value cannot be negative: $value" }
    }

    inline operator fun unaryMinus() = Weight(-value)
    inline operator fun div(other: Int): Weight = Weight(value / other)
    inline operator fun div(other: Weight): Float = value / other.value
    inline operator fun div(other: Float): Weight = Weight(value / other)
    inline operator fun times(other: Int): Weight = Weight(value * other)
    inline operator fun times(other: Float): Weight = Weight(value * other)
    inline operator fun plus(other: Weight) = Weight(this.value + other.value)
    inline operator fun minus(other: Weight) = Weight(this.value - other.value)

    override operator fun compareTo(other: Weight) = value.compareTo(other.value)
}

inline val Int.kg: Weight
    get() = Weight(this.toFloat())

inline val Float.kg: Weight
    get() = Weight(this)

inline val Double.kg: Weight
    get() = Weight(this.toFloat())
