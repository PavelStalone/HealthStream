package ru.health.stream.feature.vitals.data.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.data.model.SimpleHealthMeasurement

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
    fun heartRateMapper(simpleHeartRate: SimpleHealthMeasurement.SimpleHeartRate): HealthMeasurement.HeartRate
}
