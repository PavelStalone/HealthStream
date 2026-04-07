package ru.health.stream.data.vitals.model

import kotlinx.datetime.Instant

sealed interface Device : Resource {
    val id: String
    val status: Status
    val macAddress: String
    val lastMeasured: Instant

    data class WeightScale(
        override val id: String,
        override val status: Status,
        override val macAddress: String,
        override val lastMeasured: Instant
    ) : Device

    data class BloodPressure(
        override val id: String,
        override val status: Status,
        override val macAddress: String,
        override val lastMeasured: Instant
    ) : Device

    data class PulseOximeter(
        override val id: String,
        override val status: Status,
        override val macAddress: String,
        override val lastMeasured: Instant
    ) : Device

    enum class Status {

        ATTACHED,
        REJECTED,
        UNKNOWN,
        ;
    }
}

fun Device.copy(lastMeasured: Instant): Device = when (this) {
    is Device.BloodPressure -> copy(lastMeasured = lastMeasured)
    is Device.PulseOximeter -> copy(lastMeasured = lastMeasured)
    is Device.WeightScale -> copy(lastMeasured = lastMeasured)
}
