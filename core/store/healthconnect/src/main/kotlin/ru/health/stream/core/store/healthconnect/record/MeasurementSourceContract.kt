package ru.health.stream.core.store.healthconnect.record

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement

internal interface MeasurementSourceContract<T : HealthMeasurement> {

    suspend fun getMeasurementByRange(
        start: Instant,
        end: Instant,
    ): List<T>

    suspend fun writeMeasurement(measurement: T): Result<T>
}
