package ru.health.stream.source.local.room.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.dao.BodyWeightDao
import ru.health.stream.source.local.room.entity.asBodyWeightWithMetadata
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class BodyWeightTable @Inject constructor(
    private val dao: BodyWeightDao,
) : MeasurementTable<BodyWeight>() {

    override val type: KClass<BodyWeight> = BodyWeight::class

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>
    ): List<T> = dao.getWithoutEstimation()
        .map { metadata -> metadata.asBodyWeight() as T }

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = dao.getByRange(start = start, end = end)
        .map { entityWithMetadata -> entityWithMetadata.asBodyWeight() as T }

    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): Flow<List<T>> = dao.getFlowByRange(start = start, end = end)
        .map { entitiesWithMetadata ->
            entitiesWithMetadata.map { entityWithMetadata -> entityWithMetadata.asBodyWeight() as T }
        }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val value = measurement as BodyWeight

        dao.insert(value.asBodyWeightWithMetadata())
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        val values = measurements as List<BodyWeight>

        logV("writeMeasurements called: measurements=$values")
        val entitiesWithMetadata = values.map { value -> value.asBodyWeightWithMetadata() }
        logV("writeMeasurements called: bodyWeightWithMetadata=$entitiesWithMetadata")
        dao.insertAllBodyWeightWithMetadata(bodyWeightWithMetadata = entitiesWithMetadata)
        measurements
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurements running")
    }
}
