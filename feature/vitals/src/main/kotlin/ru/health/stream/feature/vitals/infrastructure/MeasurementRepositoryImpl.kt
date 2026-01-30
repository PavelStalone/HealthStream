package ru.health.stream.feature.vitals.infrastructure

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.mapper.asHeartRate
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
    ): List<T> = when (type) {
        HealthMeasurement.HeartRate::class -> {
            measurementStore.getHeartRateByRange(start = from, end = to)
                .map { entity -> entity.asHeartRate() as T }
        }

        else -> emptyList()
    }

    override suspend fun <T : HealthMeasurement> getMeasurementsByDuration(
        duration: Duration,
        type: KClass<T>
    ): List<T> = when (type) {
        HealthMeasurement.HeartRate::class -> {
            measurementStore.getHeartRateByDuration(duration = duration)
                .map { entity -> entity.asHeartRate() as T }
        }

        else -> emptyList()
    }

    override suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T> =
        when (measurement) {
            is HealthMeasurement.HeartRate -> {
                val heartRate = measurement.asHeartRate()

                measurementStore.writeHeartRate(heartRate)
                    .map { entity -> entity.asHeartRate() as T }
            }

            else -> Result.failure(IllegalArgumentException("Measurement type not found"))
        }
}
