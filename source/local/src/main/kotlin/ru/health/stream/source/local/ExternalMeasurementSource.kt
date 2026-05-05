package ru.health.stream.source.local

import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.infrastructure.source.local.LocalMeasurementSource
import kotlin.reflect.KClass

interface ExternalMeasurementSource : LocalMeasurementSource {

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>,
    ): List<T> = emptyList()
}
