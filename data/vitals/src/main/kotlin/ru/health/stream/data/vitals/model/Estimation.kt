package ru.health.stream.data.vitals.model

data class Estimation(
    val level: Level,
    val description: String? = null,
) : Metadata.Element {

    override val key: Metadata.Key<*> = Key

    enum class Level {

        LOW,
        NORMAL,
        HIGH,
        CRITICAL,
        ;
    }

    companion object Key : Metadata.Key<Estimation>
}

fun Estimation?.changeByPriority(other: Estimation?): Estimation? =
    when {
        this == null -> other
        other == null -> this
        else -> {
            val level = level.changeByPriority(other.level)

            if (level == other.level) other else this
        }
    }

fun Estimation.Level?.changeByPriority(other: Estimation.Level?): Estimation.Level? =
    when {
        this == null -> other
        other == null -> this
        (ordinal < other.ordinal && other != Estimation.Level.NORMAL) -> other
        else -> this
    }
