package ru.health.stream.source.local.healthconnect.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.ExternalMeasurementSource
import ru.health.stream.source.local.healthconnect.record.MeasurementSource
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class HealthConnectMeasurementSource @Inject constructor(
    private val measurementsSources: List<@JvmSuppressWildcards MeasurementSource<Measurement>>
) : ExternalMeasurementSource {

    override suspend fun <T : Measurement> getMeasurementsByRange(
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

        val measurementClass = measurement::class
        val record = measurementsSources.first { record -> measurementClass == record.type }

        record.deleteMeasurement(measurement)
        measurement
    }.onFailure { exception ->
        logW("Error while deleteMeasurement running", exception)
    }

    override suspend fun <T : Measurement> writeMeasurement(
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

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        logV("writeMeasurements called: measurements=$measurements")

        val measurementsWithType = measurements.groupBy { measurement -> measurement::class }

        measurementsWithType.forEach { (type, measurements) ->
            val record = measurementsSources.first { record -> type == record.type }

            record.writeMeasurements(measurements = measurements)
        }

        measurements
    }.onFailure { exception ->
        logW("Error while writeMeasurements running", exception)
    }
}
