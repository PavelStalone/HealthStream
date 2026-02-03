package ru.health.stream.feature.vitals.infrastructure

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.source.local.LocalHealthMeasurementSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.time.Duration

@Singleton
@Suppress("UNCHECKED_CAST")
internal class MeasurementRepositoryImpl @Inject constructor(
    private val measurementSource: LocalHealthMeasurementSource
) : MeasurementRepository {

    override suspend fun <T : HealthMeasurement> getMeasurementsByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>
    ): List<T> = measurementSource.getMeasurementByRange(start = from, end = to, type = type)

    override suspend fun <T : HealthMeasurement> getMeasurementsByDuration(
        duration: Duration,
        type: KClass<T>
    ): List<T> = measurementSource.getMeasurementByDuration(duration = duration, type = type)

    override suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T> =
        measurementSource.writeMeasurement(measurement = measurement)
}
