package ru.health.stream.feature.vitals.data.model

sealed interface Resource {

    data object Manual : Resource

    data class App(val packageName: String) : Resource

    /** [Device] : Resource */
}
