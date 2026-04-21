package ru.health.stream.source.local.file.model

internal data class Mean(
    val mean: Double,
    val count: Int = 1
) {

    fun add(value: Double): Mean = Mean(
        mean = mean + (value - mean) / (count + 1),
        count = count + 1
    )
}
