package ru.health.stream.source.local.room.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ResourceEntity {

    @Serializable
    @SerialName("Manual")
    data object Manual : ResourceEntity

    @Serializable
    @SerialName("App")
    data class App(val packageName: String) : ResourceEntity

    sealed interface DeviceEntity : ResourceEntity {
        val id: String
        val status: Status
        val macAddress: String
        val lastMeasured: Instant

        @Serializable
        @SerialName("WeightScale")
        data class WeightScale(
            override val id: String,
            override val status: Status,
            override val macAddress: String,
            override val lastMeasured: Instant
        ) : DeviceEntity

        @Serializable
        @SerialName("BloodPressure")
        data class BloodPressure(
            override val id: String,
            override val status: Status,
            override val macAddress: String,
            override val lastMeasured: Instant
        ) : DeviceEntity

        @Serializable
        @SerialName("PulseOximeter")
        data class PulseOximeter(
            override val id: String,
            override val status: Status,
            override val macAddress: String,
            override val lastMeasured: Instant
        ) : DeviceEntity

        @Serializable
        enum class Status {
            ATTACHED,
            REJECTED,
            UNKNOWN,
        }
    }
}
