package ru.health.stream.room.mapper

import ru.health.stream.feature.vitals.data.model.HeartRate
import ru.health.stream.room.entity.HeartRateEntity

internal fun HeartRate.WithResource.asHeartRateEntity() = HeartRateEntity(
    id = id,
    pulse = pulse,
    createdAt = createdAt,
    resource = resource.asResourceEntity(),
)

internal fun HeartRateEntity.asHeartRate() = HeartRate.WithResource(
    heartRate = HeartRate.Simple(
        id = id,
        createdAt = createdAt,
        pulse = pulse,
    ),
    resource = resource.asResource(),
)
