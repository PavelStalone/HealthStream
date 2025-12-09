package ru.health.stream.room.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ResourceEntity {

    @Serializable
    @SerialName("Manual")
    data object Manual : ResourceEntity

    @Serializable
    @SerialName("FromApp")
    data class FromApp(val packageName: String) : ResourceEntity

    @Serializable
    @SerialName("WeightScale")
    data class WeightScale(val manufacturer: String? = null) : ResourceEntity

    @Serializable
    @SerialName("BloodPressure")
    data class BloodPressure(val manufacturer: String? = null) : ResourceEntity

    @Serializable
    @SerialName("PulseOximeter")
    data class PulseOximeter(val manufacturer: String? = null) : ResourceEntity
}
