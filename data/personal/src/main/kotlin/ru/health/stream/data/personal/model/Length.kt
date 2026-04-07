package ru.health.stream.data.personal.model

@JvmInline
value class Length(val meters: Double) : Comparable<Length> {

    init {
        require(meters >= 0) { "Length cannot be negative: $meters" }
    }

    val cm: Double get() = meters * 100.0
    val mm: Double get() = meters * 1000.0
    val feet: Double get() = meters * 3.28084
    val inches: Double get() = meters * 39.3701

    inline operator fun plus(other: Length): Length = Length(meters + other.meters)
    inline operator fun minus(other: Length): Length = Length(meters - other.meters)
    inline operator fun times(other: Double): Length = Length(meters * other)
    inline operator fun times(other: Int): Length = Length(meters * other)
    inline operator fun div(other: Double): Length = Length(meters / other)
    inline operator fun div(other: Int): Length = Length(meters / other)

    override fun compareTo(other: Length): Int = meters.compareTo(other.meters)
}

inline val Double.meters: Length get() = Length(this)
inline val Double.cm: Length get() = Length(this / 100.0)
inline val Double.mm: Length get() = Length(this / 1000.0)
inline val Double.feet: Length get() = Length(this / 3.28084)
inline val Double.inches: Length get() = Length(this / 39.3701)

inline val Int.cm: Length get() = toDouble().cm
inline val Int.mm: Length get() = toDouble().mm
inline val Int.feet: Length get() = toDouble().feet
inline val Int.meters: Length get() = toDouble().meters
inline val Int.inches: Length get() = toDouble().inches
