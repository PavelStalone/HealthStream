package ru.health.stream.source.local

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.infrastructure.source.local.LocalMeasurementSource
import kotlin.reflect.KClass

interface PrimaryMeasurementSource : LocalMeasurementSource {

    suspend fun <T : Measurement> getAllMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T>
}
