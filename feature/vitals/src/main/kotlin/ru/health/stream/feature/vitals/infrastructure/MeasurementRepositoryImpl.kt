package ru.health.stream.feature.vitals.infrastructure

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.source.local.LocalHeartRateStore
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.time.Duration

@Singleton
@Suppress("UNCHECKED_CAST")
internal class MeasurementRepositoryImpl @Inject constructor(
    private val localHeartRateStore: LocalHeartRateStore
) : MeasurementRepository {

    override suspend fun <T : HealthMeasurement> getMeasurementsByRange(
        from: Instant,
        to: Instant,
        type: KClass<T>
    ): List<T> = when (type) {
        HealthMeasurement.HeartRate::class -> {
            localHeartRateStore.getHeartRateByRange(start = from, end = to)
                .map { entity ->
                    HealthMeasurement.HeartRate(
                        createdAt = entity.createdAt,
                        resource = entity.resource,
                        pulse = entity.pulse
                    ) as T
                }
        }

        else -> emptyList()
    }

    override suspend fun <T : HealthMeasurement> getMeasurementsByDuration(
        duration: Duration,
        type: KClass<T>
    ): List<T> = when (type) {
        HealthMeasurement.HeartRate::class -> {
            localHeartRateStore.getHeartRateByDuration(duration = duration)
                .map { entity ->
                    HealthMeasurement.HeartRate(
                        createdAt = entity.createdAt,
                        resource = entity.resource,
                        pulse = entity.pulse
                    ) as T
                }
        }

        else -> emptyList()
    }

    override suspend fun <T : HealthMeasurement> createMeasurement(measurement: T): Result<T> =
        when (measurement) {
            is HealthMeasurement.HeartRate -> {
                val heartRate = HeartRate(
                    pulse = measurement.pulse,
                    resource = measurement.resource,
                    createdAt = measurement.createdAt,
                )

                localHeartRateStore.writeHeartRate(heartRate)
                    .map { entity ->
                        HealthMeasurement.HeartRate(
                            createdAt = entity.createdAt,
                            resource = entity.resource,
                            pulse = entity.pulse
                        ) as T
                    }
            }

            else -> Result.failure(IllegalArgumentException("Measurement type not found"))
        }
}
