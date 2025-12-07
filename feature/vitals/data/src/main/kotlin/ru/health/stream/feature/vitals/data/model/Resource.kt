package ru.health.stream.feature.vitals.data.model

sealed interface Resource {

    data object WeightScale: Resource
    data object BloodPressure: Resource
    data object PulseOximeter: Resource

    data class FromApp(val appPackage: String): Resource
}
