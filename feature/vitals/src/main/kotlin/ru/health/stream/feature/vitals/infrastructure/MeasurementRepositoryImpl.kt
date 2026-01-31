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
    private val measurementStore: LocalHealthMeasurementSource
) : MeasurementRepository {

    override suspend fun <T : HealthMeasurement> getMeasurementsByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>
    ): List<T> = measurementStore.getMeasurementByRange(start = from, end = to, kClass = type)

    override suspend fun <T : HealthMeasurement> getMeasurementsByDuration(
        duration: Duration,
        type: KClass<T>
    ): List<T> = measurementStore.getMeasurementByDuration(duration = duration, kClass = type)

    override suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T> =
        measurementStore.writeMeasurement(measurement = measurement)
}
