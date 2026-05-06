package ru.health.stream.source.local.healthconnect.record

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.measurement.Measurement

internal interface MeasurementSourceContract<T : Measurement> {

    suspend fun getMeasurementByRange(
        start: Instant,
        end: Instant,
    ): List<T>

    suspend fun deleteMeasurement(measurement: T): Result<T>
    suspend fun writeMeasurement(measurement: T): Result<T>
    suspend fun writeMeasurements(measurements: List<T>): Result<List<T>>
}
