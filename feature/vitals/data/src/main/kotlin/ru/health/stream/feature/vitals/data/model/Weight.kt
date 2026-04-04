package ru.health.stream.feature.vitals.data.model

@JvmInline
value class Weight(val kg: Float) : Comparable<Weight> {

    init {
        require(kg >= 0) { "Weight value cannot be negative: $kg" }
    }

    val lb: Float get() = kg * 2.20462f

    inline operator fun div(other: Int): Weight = Weight(kg / other)
    inline operator fun div(other: Float): Weight = Weight(kg / other)
    inline operator fun times(other: Int): Weight = Weight(kg * other)
    inline operator fun times(other: Float): Weight = Weight(kg * other)
    inline operator fun plus(other: Weight) = Weight(this.kg + other.kg)
    inline operator fun minus(other: Weight) = Weight(this.kg - other.kg)

    override operator fun compareTo(other: Weight) = kg.compareTo(other.kg)
}

inline val Float.kg: Weight get() = Weight(this)
inline val Float.lb: Weight get() = Weight(this / 2.20462f)

inline val Double.kg: Weight get() = toFloat().kg
inline val Double.lb: Weight get() = toFloat().lb

inline val Int.kg: Weight get() = toFloat().kg
inline val Int.lb: Weight get() = toFloat().lb
