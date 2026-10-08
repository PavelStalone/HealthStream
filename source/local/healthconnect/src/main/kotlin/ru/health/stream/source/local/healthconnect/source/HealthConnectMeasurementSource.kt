package ru.health.stream.source.local.healthconnect.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import ru.health.stream.core.common.permission.PermissionManager
import ru.health.stream.core.common.permission.PermissionStatus
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.ExternalMeasurementSource
import ru.health.stream.source.local.healthconnect.HealthConnectManager
import ru.health.stream.source.local.healthconnect.record.MeasurementSource
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class HealthConnectMeasurementSource @Inject constructor(
    private val permissionManager: PermissionManager,
    private val healthConnectManager: HealthConnectManager,
    private val measurementsSources: List<@JvmSuppressWildcards MeasurementSource<Measurement>>
) : ExternalMeasurementSource {

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$type")

        require(healthConnectManager.isActiveHealthConnect.first()) { "HealthConnect disabled" }

        val sources =
            measurementsSources.filter { source -> type.java.isAssignableFrom(source.type.java) }
        val permissions =
            permissionManager.checkGroup(*sources.map { it.readPermission }.toTypedArray())

        val response = sources
            .filter { source -> permissions[source.readPermission] == PermissionStatus.Granted }
            .flatMap { source -> source.getMeasurementByRange(start = start, end = end) }

        logV("Founded measurements: $response")

        response as List<T>
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): Flow<List<T>> {
        logV("getMeasurementFlowByDuration called: start=$start, end=$end, kClass=$type")

        return flow { emit(getMeasurementsByRange(start = start, end = end, type = type)) }
    }

    override suspend fun <T : Measurement> deleteMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        logV("deleteMeasurement called: measurement=$measurement")

        require(healthConnectManager.isActiveHealthConnect.first()) { "HealthConnect disabled" }

        val measurementClass = measurement::class
        val source = measurementsSources.first { source -> measurementClass == source.type }
        check(permissionManager.check(source.writePermission) == PermissionStatus.Granted)

        source.deleteMeasurement(measurement).getOrThrow()
        measurement
    }.onFailure { exception ->
        logW("Error while deleteMeasurement running", exception)
    }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        logV("writeMeasurement called: measurement=$measurement")

        require(healthConnectManager.isActiveHealthConnect.first()) { "HealthConnect disabled" }

        val measurementClass = measurement::class
        val source = measurementsSources.first { source -> measurementClass == source.type }
        check(permissionManager.request(source.writePermission) == PermissionStatus.Granted)

        source.writeMeasurement(measurement).getOrThrow()
        measurement
    }.onFailure { exception ->
        logW("Error while writeMeasurement running", exception)
    }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        logV("writeMeasurements called: measurements=$measurements")

        require(healthConnectManager.isActiveHealthConnect.first()) { "HealthConnect disabled" }

        val measurementsWithType = measurements.groupBy { measurement -> measurement::class }

        val sources =
            measurementsSources.filter { source -> measurementsWithType.keys.any { it == source.type } }
        val permissions =
            permissionManager.requestGroup(*sources.map { it.writePermission }.toTypedArray())

        check(permissions.any { (_, status) -> status == PermissionStatus.Granted })

        sources.filter { source -> permissions[source.writePermission] == PermissionStatus.Granted }
            .fold(mutableListOf<T>()) { acc, source ->
                val measurements = measurementsWithType.firstNotNullOf { (type, measurements) ->
                    if (type == source.type) measurements else null
                }

                source.writeMeasurements(measurements).onSuccess { acc.addAll(measurements) }
                acc
            }
    }.onFailure { exception ->
        logW("Error while writeMeasurements running", exception)
    }
}
