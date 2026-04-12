package ru.health.stream.source.local.room.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.PrimaryMeasurementSource
import ru.health.stream.source.local.room.MeasurementTable
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class RoomMeasurementSource @Inject constructor(
    private val tables: List<@JvmSuppressWildcards MeasurementTable<Measurement>>,
) : PrimaryMeasurementSource {

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$type")

        val response = tables.filter { table -> type.java.isAssignableFrom(table.type.java) }
            .flatMap { table ->
                table.getMeasurementsByRange(
                    start = start,
                    end = end,
                    type = type,
                )
            }

        logV("Founded measurements: $response")
        response
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): Flow<List<T>> = runCatching {
        logV("getMeasurementFlowByRange called: start=$start, end=$end, kClass=$type")

        val response = tables.filter { table -> type.java.isAssignableFrom(table.type.java) }
            .map { table ->
                table.getMeasurementsFlowByRange(
                    start = start,
                    end = end,
                    type = type,
                )
            }
            .let { flows ->
                if (flows.isEmpty()) return@let flowOf(emptyList())
                combine(flows) { measurements -> measurements.reduce(Collection<*>::plus) as List<T> }
            }

        logV("Founded measurements: $response")
        response
    }.onFailure { exception ->
        logW("Error while getMeasurementFlowByRange running", exception)
    }.getOrElse { flowOf(emptyList()) }

    override suspend fun <T : Measurement> writeMeasurement(measurement: T): Result<T> =
        runCatching {
            logV("writeMeasurement called: measurement=$measurement")

            val measurementClass = measurement::class
            val table = tables.first { table -> measurementClass == table.type }

            table.writeMeasurement(measurement).getOrThrow()
        }.onFailure { exception ->
            logW("Error while writeMeasurement running", exception)
        }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        logV("writeMeasurements called: measurements=$measurements")

        val measurementsWithType = measurements.groupBy { measurement -> measurement::class }

        measurementsWithType.forEach { (type, measurements) ->
            val table = tables.first { table -> type == table.type }

            table.writeMeasurements(measurements)
        }

        measurements
    }.onFailure { exception ->
        logW("Error while writeMeasurements running", exception)
    }
}
