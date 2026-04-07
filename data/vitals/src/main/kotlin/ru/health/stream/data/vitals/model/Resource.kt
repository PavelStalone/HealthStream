package ru.health.stream.data.vitals.model

sealed interface Resource {

    data object Manual : Resource

    data class App(val packageName: String) : Resource

    /** [Device] : Resource */
}
