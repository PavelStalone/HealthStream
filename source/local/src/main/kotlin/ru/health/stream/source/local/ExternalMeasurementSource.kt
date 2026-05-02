package ru.health.stream.source.local

import ru.health.stream.data.vitals.api.local.LocalMeasurementSource
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

interface ExternalMeasurementSource : LocalMeasurementSource {

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>,
    ): List<T> = emptyList()
}
