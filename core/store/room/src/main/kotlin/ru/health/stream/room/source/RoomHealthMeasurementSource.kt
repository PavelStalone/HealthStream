package ru.health.stream.room.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.vitals.HealthMeasurementSource
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.room.MeasurementTable
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class RoomHealthMeasurementSource @Inject constructor(
    private val tables: List<@JvmSuppressWildcards MeasurementTable<HealthMeasurement, *>>,
) : HealthMeasurementSource {

    override val isActive: Flow<Boolean> = flowOf(true)

    override suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$type")

        val response = tables.filter { table -> type.java.isAssignableFrom(table.type.java) }
            .flatMap { table ->
                table.getByRange(start = start, end = end).map { entity ->
                    table.mapToMeasurement(entity)
                }
            }

        logV("Founded measurements: $response")

        response as List<T>
    }.onFailure { exception ->
        logW("Error while getMeasurementByRange running", exception)
    }.getOrElse { emptyList() }

    override fun <T : HealthMeasurement> getMeasurementFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): Flow<List<T>> = runCatching {
        logV("getMeasurementFlowByRange called: start=$start, end=$end, kClass=$type")

        val response = tables.filter { table -> type.java.isAssignableFrom(table.type.java) }
            .map { table ->
                table.getFlowByRange(start = start, end = end).map { entities ->
                    entities.map { entity -> table.mapToMeasurement(entity) }
                }
            }
            .let { flows ->
                combine(flows) { measurements -> measurements.reduce(Collection<*>::plus) as List<T> }
            }

        logV("Founded measurements: $response")

        response
    }.onFailure { exception ->
        logW("Error while getMeasurementFlowByRange running", exception)
    }.getOrElse { flowOf(emptyList()) }

    override suspend fun <T : HealthMeasurement> writeMeasurement(measurement: T): Result<T> =
        runCatching {
            logV("writeMeasurement called: measurement=$measurement")

            val measurementClass = measurement::class
            val table = tables.first { table -> measurementClass == table.type }

            table.insert(measurement)
            measurement
        }.onFailure { exception ->
            logW("Error while writeMeasurement running", exception)
        }
}
