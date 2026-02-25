package ru.health.stream.feature.vitals.source.local

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import kotlin.reflect.KClass
import kotlin.time.Duration

interface LocalHealthMeasurementSource {

    suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T>

    suspend fun <T : HealthMeasurement> getMeasurementByDuration(
        duration: Duration,
        type: KClass<T>,
    ): List<T>

    fun <T : HealthMeasurement> getMeasurementFlowByDuration(
        duration: Duration,
        type: KClass<T>,
    ): Flow<List<T>>

    suspend fun <T: HealthMeasurement> writeMeasurement(measurement: T): Result<T>
}
