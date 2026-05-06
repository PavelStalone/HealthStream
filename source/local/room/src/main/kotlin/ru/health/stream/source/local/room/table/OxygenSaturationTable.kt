package ru.health.stream.source.local.room.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.dao.OxygenSaturationDao
import ru.health.stream.source.local.room.entity.asOxygenSaturationWithMetadata
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class OxygenSaturationTable @Inject constructor(
    private val dao: OxygenSaturationDao,
) : MeasurementTable<OxygenSaturation>() {

    override val type: KClass<OxygenSaturation> = OxygenSaturation::class

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>
    ): List<T> = dao.getWithoutEstimation()
        .map { metadata -> metadata.asOxygenSaturation() as T }

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = dao.getByRange(start = start, end = end)
        .map { entityWithMetadata -> entityWithMetadata.asOxygenSaturation() as T }

    override suspend fun <T : Measurement> getAllMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = dao.getAllByRange(start = start, end = end)
        .map { entityWithMetadata -> entityWithMetadata.asOxygenSaturation() as T }

    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): Flow<List<T>> = dao.getFlowByRange(start = start, end = end)
        .map { entitiesWithMetadata ->
            entitiesWithMetadata.map { entityWithMetadata -> entityWithMetadata.asOxygenSaturation() as T }
        }

    override suspend fun <T : Measurement> deleteMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val value = measurement as OxygenSaturation

        val spoWithMetadata = value.asOxygenSaturationWithMetadata()

        dao.insert(
            spoWithMetadata.copy(
                oxygenSaturationEntity = spoWithMetadata.oxygenSaturationEntity.copy(isRemoved = true)
            )
        )
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while deleteMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val value = measurement as OxygenSaturation

        dao.insert(value.asOxygenSaturationWithMetadata())
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        val values = measurements as List<OxygenSaturation>

        logV("writeMeasurements called: measurements=$values")
        val entitiesWithMetadata = values.map { value -> value.asOxygenSaturationWithMetadata() }
        logV("writeMeasurements called: oxygenSaturationWithMetadata=$entitiesWithMetadata")
        dao.insertAllOxygenSaturationWithMetadata(oxygenSaturationWithMetadata = entitiesWithMetadata)
        measurements
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurements running")
    }
}
