package ru.health.stream.room.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.room.entity.HeartRateEntity
import ru.health.stream.room.entity.ResourceEntity

@AutoMapperModule
internal interface MapperModule {

    @AutoMapper
    fun heartRateMapper(heartRate: HeartRate): HeartRateEntity

    @AutoMapper
    fun resourceMapper(resource: Resource): ResourceEntity
}
