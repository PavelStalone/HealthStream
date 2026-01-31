package ru.health.stream.room.source

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.vitals.HealthMeasurementSource
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.room.MeasurementTable
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration

@Suppress("UNCHECKED_CAST")
internal class RoomHealthMeasurementSource @Inject constructor(
    private val tables: List<@JvmSuppressWildcards MeasurementTable<HealthMeasurement, *>>,
) : HealthMeasurementSource {

    override suspend fun isActive(): Boolean = true

    override suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        kClass: KClass<T>
    ): List<T> = runCatching {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$kClass")

        val response = tables.filter { table -> kClass.java.isAssignableFrom(table.kClass.java) }
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

    override suspend fun <T : HealthMeasurement> getMeasurementByDuration(
        duration: Duration,
        kClass: KClass<T>
    ): List<T> {
        logV("getMeasurementByDuration called: duration=$duration, kClass=$kClass")

        val now = Clock.System.now()

        return getMeasurementByRange(start = now - duration, end = now, kClass = kClass)
    }

    override suspend fun <T : HealthMeasurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        logV("writeMeasurement called: measurement=$measurement")

        val measurementClass = measurement::class
        val table = tables.first { table -> measurementClass == table.kClass }

        table.insert(measurement)
        measurement
    }.onFailure { exception ->
        logW("Error while writeMeasurement running", exception)
    }
}
