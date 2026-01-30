package ru.health.stream.feature.vitals.source.local

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import kotlin.reflect.KClass
import kotlin.time.Duration

interface LocalHealthMeasurementSource {

    suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        kClass: KClass<T>,
    ): List<T>

    suspend fun <T : HealthMeasurement> getMeasurementByDuration(
        duration: Duration,
        kClass: KClass<T>,
    ): List<T>

    suspend fun <T: HealthMeasurement> writeMeasurement(measurement: T): Result<T>
}
