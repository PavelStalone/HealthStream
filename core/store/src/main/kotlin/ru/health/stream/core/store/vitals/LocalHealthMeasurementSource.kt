package ru.health.stream.core.store.vitals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.health.stream.core.common.bindByFlow
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.store.NotAvailableStoreException
import ru.health.stream.core.store.Store
import ru.health.stream.core.store.mergeByTimeAndId
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.feature.vitals.source.local.LocalHealthMeasurementSource
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration

interface HealthMeasurementSource : Store, LocalHealthMeasurementSource

internal class LocalHealthMeasurementSourceImpl @Inject constructor(
    sources: Set<@JvmSuppressWildcards HealthMeasurementSource>
) : LocalHealthMeasurementSource {

    private val activeSources = sources.bindByFlow { item -> item.isActive }

    override suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$type")

        return activeSources.first()
            .map { source ->
                logD("${source::class.simpleName} isActive")

                source.getMeasurementByRange(start = start, end = end, type = type)
            }
            .mergeByTimeAndId()
            .toList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T : HealthMeasurement> getMeasurementFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): Flow<List<T>> {
        logV("getMeasurementFlowByRange called: start=$start, end=$end, kClass=$type")

        return activeSources.flatMapLatest { sources ->
            sources.map { source ->
                logD("${source::class.simpleName} isActive")

                source.getMeasurementFlowByRange(start = start, end = end, type = type)
            }.let { flows ->
                combine(flows) { measurements -> measurements.toList().mergeByTimeAndId().toList() }
            }
        }
    }

    override suspend fun <T : HealthMeasurement> writeMeasurement(measurement: T): Result<T> {
        logV("writeMeasurement called: measurement=$measurement")

        return activeSources.first()
            .fold(initial = Result.failure(NotAvailableStoreException())) { acc, healthMeasurementSource ->
                val sourceName = healthMeasurementSource::class.simpleName
                logD("$sourceName isActive")

                val result = healthMeasurementSource.writeMeasurement(measurement)

                logD("$sourceName write: ${result.isSuccess}")

                if (acc.isSuccess) return@fold acc
                result
            }
    }
}
