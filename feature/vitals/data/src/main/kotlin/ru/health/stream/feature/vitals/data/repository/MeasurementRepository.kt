package ru.health.stream.feature.vitals.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.Period
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import kotlin.reflect.KClass
import kotlin.time.Duration

interface MeasurementRepository {

    suspend fun <T : HealthMeasurement> getMeasurementsByRange(
        type: KClass<T>,
        from: Instant,
        to: Instant,
    ): List<T>

    fun <T : HealthMeasurement> getMeasurementsFlowByRange(
        type: KClass<T>,
        from: Instant,
        to: Instant,
    ): Flow<List<T>>

    suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T>
}
