package ru.health.stream.room.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.DefaultValue
import ru.health.stream.feature.personal.data.model.User
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import ru.health.stream.room.converter.EmailConverter
import ru.health.stream.room.converter.LengthConverter
import ru.health.stream.room.converter.LocalDateConverter
import ru.health.stream.room.entity.HeartRateEntity
import ru.health.stream.room.entity.ResourceEntity
import ru.health.stream.room.entity.UserEntity

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
