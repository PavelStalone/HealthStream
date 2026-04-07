package ru.health.stream.source.local

import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.api.local.LocalMeasurementSource
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.copy
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

internal class SyncableMeasurementLocalSource @Inject constructor(
    private val primarySource: PrimaryMeasurementSource,
    private val externalSources: Set<@JvmSuppressWildcards ExternalMeasurementSource>,
) : LocalMeasurementSource {

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T> {
        val allExternal = externalSources.flatMap { src ->
            src.getMeasurementsByRange(start = start, end = end, type = type)
        }

        syncExternalToPrimary(
            start = start,
            end = end,
            type = type,
            externalMeasurements = allExternal,
        )

        return primarySource.getMeasurementsByRange(start = start, end = end, type = type)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): Flow<List<T>> {
        if (externalSources.isEmpty()) {
            return primarySource.getMeasurementsFlowByRange(start, end, type)
        }

        val externalFlows = externalSources.map { src ->
            src.getMeasurementsFlowByRange(start = start, end = end, type = type)
        }

        return combine(externalFlows) { flowData -> flowData.flatMap { measurements -> measurements } }
            .distinctUntilChanged()
            .transformLatest { allExternal ->
                // Синхронизируем внешние данные в основной источник.
                // При записи в основной источник (primarySource), его Flow обновится автоматически.
                syncExternalToPrimary(
                    start = start,
                    end = end,
                    type = type,
                    externalMeasurements = allExternal,
                )

                // Передаем управление Flow из основного источника.
                // Все изменения (включая только что добавленные) придут через него.
                emitAll(primarySource.getMeasurementsFlowByRange(start, end, type))
            }
            .distinctUntilChanged()
    }

    private suspend fun <T : Measurement> syncExternalToPrimary(
        externalMeasurements: List<T>,
        start: Instant,
        end: Instant,
        type: KClass<T>
    ) {
        if (externalMeasurements.isEmpty()) return

        val primaryMeasurements = primarySource.getMeasurementsByRange(start, end, type)
        val primaryTimeSet = primaryMeasurements.map { it.createdAt }.toMutableSet()
        val primaryIdSet = primaryMeasurements.map { it.id }.toMutableSet()

        externalMeasurements.forEach { external ->
            if (!primaryTimeSet.contains(external.createdAt)) {
                val measurementToSave = if (primaryIdSet.contains(external.id)) {
                    @Suppress("UNCHECKED_CAST")
                    external.copy(id = Uuid.random().toString()) as T
                } else {
                    external
                }

                primaryTimeSet.add(measurementToSave.createdAt)
                primaryIdSet.add(measurementToSave.id)

                primarySource.writeMeasurement(measurementToSave)
            }
        }
    }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> {
        externalSources.forEach { src -> src.writeMeasurement(measurement) }

        return primarySource.writeMeasurement(measurement)
    }
}
