package ru.health.stream.core.store.healthconnect.store

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.healthconnect.HealthConnectManager
import ru.health.stream.core.store.healthconnect.record.MeasurementSource
import ru.health.stream.core.store.vitals.HealthMeasurementSource
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Suppress("UNCHECKED_CAST")
internal class HealthConnectMeasurementSource @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val measurementsSources: List<@JvmSuppressWildcards MeasurementSource<HealthMeasurement>>
) : HealthMeasurementSource {

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
    )

    override suspend fun isActive(): Boolean = healthConnectManager.hasAllPermissions(PERMISSIONS)

    override suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$type")

        val response = measurementsSources.filter { record -> type.java.isAssignableFrom(record.type.java) }
            .flatMap { record ->
                record.getMeasurementByRange(start = start, end = end)
            }

        logV("Founded measurements: $response")

        response as List<T>
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override suspend fun <T : HealthMeasurement> getMeasurementByDuration(
        duration: Duration,
        type: KClass<T>
    ): List<T> {
        logV("getMeasurementByDuration called: duration=$duration, type=$type")

        val now = Clock.System.now()

        return getMeasurementByRange(start = now - duration, end = now, type = type)
    }

    override suspend fun <T : HealthMeasurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        logV("writeMeasurement called: measurement=$measurement")

        val measurementClass = measurement::class
        val record = measurementsSources.first { record -> measurementClass == record.type }

        record.writeMeasurement(measurement)
        measurement
    }.onFailure { exception ->
        logW("Error while writeMeasurement running", exception)
    }
}
