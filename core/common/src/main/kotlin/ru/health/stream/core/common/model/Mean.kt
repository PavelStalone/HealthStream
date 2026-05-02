package ru.health.stream.core.common.model

// TODO: Transform to value class with package Float + Int - shoplikpavel 2026-04-29
data class Mean(
    val value: Double,
    val count: Int = 1
) {

    fun add(value: Double): Mean = Mean(
        value = this@Mean.value + (value - this@Mean.value) / (count + 1),
        count = count + 1
    )
}
