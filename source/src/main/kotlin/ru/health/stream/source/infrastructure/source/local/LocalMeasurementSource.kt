package ru.health.stream.source.infrastructure.source.local

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

interface LocalMeasurementSource {

    suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>,
    ): List<T>

    suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T>

    fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): Flow<List<T>>

    suspend fun <T : Measurement> deleteMeasurement(measurement: T): Result<T>
    suspend fun <T : Measurement> writeMeasurement(measurement: T): Result<T>
    suspend fun <T : Measurement> writeMeasurements(measurements: List<T>): Result<List<T>>
}
