package ru.health.stream.core.store.healthconnect.source

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.healthconnect.HealthConnectManager
import ru.health.stream.core.store.healthconnect.record.MeasurementSource
import ru.health.stream.core.store.vitals.HealthMeasurementSource
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class HealthConnectMeasurementSource @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val measurementsSources: List<@JvmSuppressWildcards MeasurementSource<HealthMeasurement>>
) : HealthMeasurementSource {

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
    )

    override val isActive: Flow<Boolean> = flow {
        emit(healthConnectManager.hasAllPermissions(PERMISSIONS))
    }

    override suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$type")

        val response = measurementsSources
            .filter { record -> type.java.isAssignableFrom(record.type.java) }
            .flatMap { record -> record.getMeasurementByRange(start = start, end = end) }

        logV("Founded measurements: $response")

        response as List<T>
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override fun <T : HealthMeasurement> getMeasurementFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): Flow<List<T>> {
        logV("getMeasurementFlowByDuration called: start=$start, end=$end, kClass=$type")

        return flow { emit(getMeasurementByRange(start = start, end = end, type = type)) }
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
