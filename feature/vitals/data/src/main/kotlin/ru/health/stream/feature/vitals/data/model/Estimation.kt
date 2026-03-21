package ru.health.stream.feature.vitals.data.model

data class MeasurementEstimation(
    val estimation: Estimation,
    val description: String? = null,
) : Metadata.Element {

    override val key: Metadata.Key<*> = Key

    companion object Key : Metadata.Key<MeasurementEstimation>
}

enum class Estimation {

    LOW,
    NORMAL,
    HIGH,
    ;
}
