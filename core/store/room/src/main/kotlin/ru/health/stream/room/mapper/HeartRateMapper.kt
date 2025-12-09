package ru.health.stream.room.mapper

import ru.health.stream.feature.vitals.source.local.model.HeartRate
import ru.health.stream.room.entity.HeartRateEntity

internal fun HeartRateEntity.asSource(): HeartRate = HeartRate(
    pulse = pulse,
    createdAt = createdAt,
    resource = resource.asSource()
)

internal fun HeartRate.asEntity(): HeartRateEntity = HeartRateEntity(
    pulse = pulse,
    createdAt = createdAt,
    resource = resource.asEntity()
)
