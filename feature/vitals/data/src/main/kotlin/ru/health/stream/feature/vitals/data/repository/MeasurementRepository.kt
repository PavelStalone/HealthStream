package ru.health.stream.feature.vitals.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import kotlin.reflect.KClass
import kotlin.time.Duration

interface MeasurementRepository {

    suspend fun <T : HealthMeasurement> getMeasurementsByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>
    ): List<T>

    suspend fun <T : HealthMeasurement> getMeasurementsByDuration(
        duration: Duration,
        type: KClass<T>
    ): List<T>

    fun <T : HealthMeasurement> getMeasurementsFlowByDuration(
        duration: Duration,
        type: KClass<T>
    ): Flow<List<T>>

    suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T>
}
