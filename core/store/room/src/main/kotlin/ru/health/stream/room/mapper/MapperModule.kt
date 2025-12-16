package ru.health.stream.room.mapper

import ru.health.stream.automapper.annotation.AutoMapper
import ru.health.stream.automapper.annotation.AutoMapperModule
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import ru.health.stream.room.entity.HeartRateEntity
import ru.health.stream.room.entity.ResourceEntity

@AutoMapperModule
internal interface MapperModule {

    @AutoMapper
    fun heartRateMapper(heartRate: HeartRate): HeartRateEntity

    @AutoMapper
    fun resourceMapper(resource: Resource): ResourceEntity
}
