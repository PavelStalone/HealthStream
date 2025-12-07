package ru.health.stream.feature.vitals.source.local.model

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.Resource

data class HeartRate(
    val createdAt: Instant,
    val resources: Resource,
    val pulse: Int,
)
