package ru.health.stream.source.local.room.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.Resource
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.source.local.room.converter.EmailConverter
import ru.health.stream.source.local.room.converter.LengthConverter
import ru.health.stream.source.local.room.converter.LocalDateConverter
import ru.health.stream.source.local.room.entity.HeartRateEntity
import ru.health.stream.source.local.room.entity.ResourceEntity
import ru.health.stream.source.local.room.entity.UserEntity

@AutoMapperModule
internal interface MapperModule {

    @AutoMapper
    fun resourceMapper(resource: Resource): ResourceEntity

    @AutoMapper
    fun heartRateMapper(heartRate: HeartRate): HeartRateEntity

    @AutoMapper
    fun statusMapper(status: Device.Status): ResourceEntity.DeviceEntity.Status

    @AutoMapper(
        converters = [
            EmailConverter::class,
            LengthConverter::class,
            LocalDateConverter::class,
        ],
    )
    fun userMapper(user: User): UserEntity
}
