package ru.health.stream.source.local

import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.health.stream.core.common.di.Dispatcher
import ru.health.stream.data.vitals.api.local.LocalMeasurementSource
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.copy
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

internal class SyncableMeasurementLocalSource @Inject constructor(
    private val primarySource: PrimaryMeasurementSource,
    private val externalSources: Set<@JvmSuppressWildcards ExternalMeasurementSource>,
    @param:Dispatcher(Dispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : LocalMeasurementSource {

    private val mutex = Mutex()

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>
    ): List<T> = withContext(ioDispatcher) {
        primarySource.getMeasurementsWithoutEstimation(type = type)
    }

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>,
    ): List<T> = withContext(ioDispatcher) {
        val allExternal = externalSources.flatMap { src ->
            src.getMeasurementsByRange(start = start, end = end, type = type)
        }

        syncExternalToPrimary(
            start = start,
            end = end,
            type = type,
            externalMeasurements = allExternal,
        )

        primarySource.getMeasurementsByRange(start = start, end = end, type = type)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): Flow<List<T>> = channelFlow {
        // Запускаем фоновую синхронизацию для каждого внешнего источника независимо
        externalSources.forEach { src ->
            launch {
                src.getMeasurementsFlowByRange(start, end, type)
                    .distinctUntilChanged()
                    .collectLatest { externalMeasurements ->
                        // Синхронизируем внешние данные. При записи в primarySource, основной коллектор (ниже) автоматически получит обновления
                        syncExternalToPrimary(
                            start = start,
                            end = end,
                            type = type,
                            externalMeasurements = externalMeasurements,
                        )
                    }
            }
        }

        launch {
            primarySource.getMeasurementsFlowByRange(start, end, type)
                .collect { measurements -> send(measurements) }
        }

        awaitClose {}
    }.flowOn(ioDispatcher)

    private suspend fun <T : Measurement> syncExternalToPrimary(
        externalMeasurements: List<T>,
        start: Instant,
        end: Instant,
        type: KClass<T>
    ) = withContext(ioDispatcher) {
        if (externalMeasurements.isEmpty()) return@withContext

        mutex.withLock {
            val primaryMeasurements = primarySource.getMeasurementsByRange(start, end, type)
            val primaryTimeSet = primaryMeasurements.map { it.createdAt }.toMutableSet()
            val primaryIdSet = primaryMeasurements.map { it.id }.toMutableSet()

            val measurementsToSave = externalMeasurements.mapNotNull { external ->
                if (primaryTimeSet.contains(external.createdAt)) return@mapNotNull null

                val measurementToSave = if (primaryIdSet.contains(external.id)) {
                    @Suppress("UNCHECKED_CAST")
                    external.copy(id = Uuid.random().toString()) as T
                } else {
                    external
                }

                primaryTimeSet.add(measurementToSave.createdAt)
                primaryIdSet.add(measurementToSave.id)
                measurementToSave
            }

            if (measurementsToSave.isNotEmpty()) {
                primarySource.writeMeasurements(measurementsToSave)
            }
        }
    }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> = withContext(ioDispatcher) {
        externalSources.forEach { src -> src.writeMeasurement(measurement) }

        primarySource.writeMeasurement(measurement)
    }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = withContext(ioDispatcher) {
        externalSources.forEach { src -> src.writeMeasurements(measurements) }

        primarySource.writeMeasurements(measurements)
    }
}
