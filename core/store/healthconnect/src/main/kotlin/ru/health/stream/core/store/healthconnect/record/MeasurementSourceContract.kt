package ru.health.stream.core.store.healthconnect.record

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import kotlin.time.Duration

interface MeasurementSourceContract<T : HealthMeasurement.WithResource> {

    suspend fun getMeasurementByRange(
        start: Instant,
        end: Instant,
    ): List<T>

    suspend fun getMeasurementByDuration(
        duration: Duration,
    ): List<T>

    suspend fun writeMeasurement(measurement: T): Result<T>
}
