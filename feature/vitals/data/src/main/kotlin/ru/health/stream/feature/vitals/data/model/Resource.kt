package ru.health.stream.feature.vitals.data.model

sealed interface Resource {

    data object Manual : Resource

    data class FromApp(val packageName: String) : Resource

    sealed interface WithManufacturer : Resource {
        val manufacturer: String?

        data class WeightScale(override val manufacturer: String? = null) : WithManufacturer
        data class BloodPressure(override val manufacturer: String? = null) : WithManufacturer
        data class PulseOximeter(override val manufacturer: String? = null) : WithManufacturer
    }
}
