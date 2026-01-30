package ru.health.stream.feature.vitals.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import ru.health.stream.feature.vitals.data.model.HealthMeasurement

@AutoMapperModule
internal interface MapperModule {

    @AutoMapper
    fun heartRateMapper(heartRate: HeartRate): HealthMeasurement.HeartRate
}
