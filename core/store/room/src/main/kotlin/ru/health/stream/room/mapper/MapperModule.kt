package ru.health.stream.room.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.data.model.HeartRate
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.room.entity.HeartRateEntity
import ru.health.stream.room.entity.ResourceEntity

@AutoMapperModule
internal interface MapperModule {

    @AutoMapper
    fun resourceMapper(resource: Resource): ResourceEntity

    @AutoMapper
    fun statusMapper(status: Device.Status): ResourceEntity.DeviceEntity.Status
}
