package ru.health.stream.source.local.room.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.Note
import ru.health.stream.source.local.room.converter.EmailConverter
import ru.health.stream.source.local.room.converter.LengthConverter
import ru.health.stream.source.local.room.converter.LocalDateConverter
import ru.health.stream.source.local.room.entity.EstimationEntity
import ru.health.stream.source.local.room.entity.NoteEntity
import ru.health.stream.source.local.room.entity.UserEntity
import ru.health.stream.source.local.room.entity.resource.DeviceEntity

@AutoMapperModule
internal interface MapperModule {

//    @AutoMapper
//    fun heartRateMapper(heartRate: HeartRate): HeartRateEntity

    @AutoMapper
    fun noteMapper(noteEntity: NoteEntity): Note

    @AutoMapper
    fun statusMapper(status: Device.Status): DeviceEntity.Status

    @AutoMapper(reversible = false)
    fun weightScaleMapper(deviceEntity: DeviceEntity): Device.WeightScale

    @AutoMapper(reversible = false)
    fun pulseOximeterMapper(deviceEntity: DeviceEntity): Device.PulseOximeter

    @AutoMapper(reversible = false)
    fun bloodPressureCuffMapper(deviceEntity: DeviceEntity): Device.BloodPressureCuff

    @AutoMapper
    fun estimationLevelMapper(estimationEntityLevel: EstimationEntity.Level): Estimation.Level

    @AutoMapper
    fun estimationMapper(estimationEntity: EstimationEntity): Estimation

    @AutoMapper(
        converters = [
            EmailConverter::class,
            LengthConverter::class,
            LocalDateConverter::class,
        ],
    )
    fun userMapper(user: User): UserEntity
}
