package ru.health.stream.data.vitals.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

interface MeasurementRepository {

    suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>,
    ): List<T>

    suspend fun <T : Measurement> getMeasurementsByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>,
    ): List<T>

    fun <T : Measurement> getMeasurementsFlowByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>,
    ): Flow<List<T>>

    suspend fun <T : Measurement> createMeasurement(measurement: T): Result<T>
}
