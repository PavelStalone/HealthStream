package ru.health.stream.data.vitals.repository.impl

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.api.local.LocalMeasurementSource
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.repository.MeasurementRepository
import kotlin.reflect.KClass

internal class MeasurementRepositoryImpl @Inject constructor(
    private val localMeasurementSource: LocalMeasurementSource,
) : MeasurementRepository {

    override suspend fun <T : Measurement> getMeasurementsByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>,
    ): List<T> = localMeasurementSource.getMeasurementsByRange(
        start = from,
        end = to,
        type = type,
    )

    override fun <T : Measurement> getMeasurementsFlowByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>,
    ): Flow<List<T>> = localMeasurementSource.getMeasurementsFlowByRange(
        start = from,
        end = to,
        type = type,
    )

    override suspend fun <T : Measurement> createMeasurement(
        measurement: T
    ): Result<T> = localMeasurementSource.writeMeasurement(
        measurement = measurement
    )
}
