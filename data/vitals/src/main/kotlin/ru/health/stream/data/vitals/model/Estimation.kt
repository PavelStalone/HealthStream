package ru.health.stream.data.vitals.model

data class Estimation(
    val estimation: Level,
    val description: String? = null,
) : Metadata.Element {

    override val key: Metadata.Key<*> = Key

    enum class Level {

        LOW,
        NORMAL,
        HIGH,
        ;
    }

    companion object Key : Metadata.Key<Estimation>
}
