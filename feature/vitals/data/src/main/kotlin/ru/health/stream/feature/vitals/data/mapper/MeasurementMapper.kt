package ru.health.stream.feature.vitals.data.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import ru.health.stream.feature.vitals.data.model.HealthMeasurement

@AutoMapperModule
internal interface MeasurementMapper {

    @AutoMapper(
        reversible = false,
        defaultValues = [
            DefaultValue(
                property = "resource",
                source = DefaultValueSource.PARAMETER
            )
        ],
    )
    fun heartRateMapper(simpleHeartRate: HealthMeasurement.SimpleHeartRate): HealthMeasurement.HeartRate
}
