package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

sealed interface BodyWeight : HealthMeasurement {

    val weight: Weight

    data class Simple(
        override val id: String,
        override val weight: Weight,
        override val createdAt: Instant
    ) : BodyWeight

    data class WithResource(
        override val resource: Resource,
        private val bodyWeight: BodyWeight,
    ) : BodyWeight by bodyWeight, HealthMeasurement.WithResource
}

fun BodyWeight.Simple.addResource(resource: Resource): BodyWeight.WithResource =
    BodyWeight.WithResource(
        bodyWeight = this,
        resource = resource,
    )

@JvmInline
value class Weight(val value: Float) : Comparable<Weight> {

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

inline val Double.kg: Weight
    get() = Weight(this.toFloat())

inline val Float.kg: Weight
    get() = Weight(this)
