package ru.health.stream.source.local.room.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.dao.BloodPressureDao
import ru.health.stream.source.local.room.entity.asBloodPressureWithMetadata
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class BloodPressureTable @Inject constructor(
    private val dao: BloodPressureDao,
) : MeasurementTable<BloodPressure>() {

    override val type: KClass<BloodPressure> = BloodPressure::class

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>
    ): List<T> = dao.getWithoutEstimation()
        .map { metadata -> metadata.asBloodPressure() as T }

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = dao.getByRange(start = start, end = end)
        .map { entityWithMetadata -> entityWithMetadata.asBloodPressure() as T }

    override suspend fun <T : Measurement> getAllMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = dao.getAllByRange(start = start, end = end)
        .map { entityWithMetadata -> entityWithMetadata.asBloodPressure() as T }

    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): Flow<List<T>> = dao.getFlowByRange(start = start, end = end)
        .map { entitiesWithMetadata ->
            entitiesWithMetadata.map { entityWithMetadata -> entityWithMetadata.asBloodPressure() as T }
        }

    override suspend fun <T : Measurement> deleteMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val value = measurement as BloodPressure

        val bpWithMetadata = value.asBloodPressureWithMetadata()

        dao.insert(
            bpWithMetadata.copy(
                bloodPressureEntity = bpWithMetadata.bloodPressureEntity.copy(isRemoved = true)
            )
        )
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while deleteMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val value = measurement as BloodPressure

        dao.insert(value.asBloodPressureWithMetadata())
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        val values = measurements as List<BloodPressure>

        logV("writeMeasurements called: measurements=$values")
        val entitiesWithMetadata = values.map { value -> value.asBloodPressureWithMetadata() }
        logV("writeMeasurements called: bloodPressureWithMetadata=$entitiesWithMetadata")
        dao.insertAllBloodPressureWithMetadata(bloodPressuresWithMetadata = entitiesWithMetadata)
        measurements
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurements running")
    }
}
