package ru.health.stream.feature.vitals.infrastructure

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.health.stream.core.common.di.Dispatcher
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.source.local.LocalHealthMeasurementSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
@Suppress("UNCHECKED_CAST")
internal class MeasurementRepositoryImpl @Inject constructor(
    private val measurementSource: LocalHealthMeasurementSource,
    @Dispatcher(Dispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : MeasurementRepository {

    override suspend fun <T : HealthMeasurement> getMeasurementsByRange(
        type: KClass<T>,
        from: Instant,
        to: Instant,
    ): List<T> = withContext(ioDispatcher) {
        measurementSource.getMeasurementByRange(start = from, end = to, type = type)
    }

    override fun <T : HealthMeasurement> getMeasurementsFlowByRange(
        type: KClass<T>,
        from: Instant,
        to: Instant,
    ): Flow<List<T>> = measurementSource.getMeasurementFlowByRange(
        start = from,
        end = to,
        type = type,
    )

    override suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T> =
        measurementSource.writeMeasurement(measurement = measurement)
}
