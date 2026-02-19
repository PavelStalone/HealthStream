package ru.health.stream.feature.vitals.source.local

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import kotlin.reflect.KClass
import kotlin.time.Duration

interface LocalHealthMeasurementSource {

    suspend fun <T : HealthMeasurement.WithResource> getMeasurementByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T>

    suspend fun <T : HealthMeasurement.WithResource> getMeasurementByDuration(
        duration: Duration,
        type: KClass<T>,
    ): List<T>

    suspend fun <T: HealthMeasurement.WithResource> writeMeasurement(measurement: T): Result<T>
}
