package ru.health.stream.source.local.room.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.dao.HeartRateDao
import ru.health.stream.source.local.room.entity.asHeartRateWithMetadata
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class HeartRateTable @Inject constructor(
    private val heartRateDao: HeartRateDao,
) : MeasurementTable<HeartRate>() {

    override val type: KClass<HeartRate> = HeartRate::class

    override suspend fun <T : Measurement> getMeasurementsWithoutEstimation(
        type: KClass<T>
    ): List<T> = heartRateDao.getWithoutEstimation()
        .map { heartRateWithMetadata -> heartRateWithMetadata.asHeartRate() as T }

    override suspend fun <T : Measurement> getMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = heartRateDao.getByRange(start = start, end = end)
        .map { heartRateWithMetadata -> heartRateWithMetadata.asHeartRate() as T }

    override suspend fun <T : Measurement> getAllMeasurementsByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): List<T> = heartRateDao.getAllByRange(start = start, end = end)
        .map { entityWithMetadata -> entityWithMetadata.asHeartRate() as T }

    override fun <T : Measurement> getMeasurementsFlowByRange(
        start: Instant,
        end: Instant,
        type: KClass<T>
    ): Flow<List<T>> = heartRateDao.getFlowByRange(start = start, end = end)
        .map { heartRatesWithMetadata ->
            heartRatesWithMetadata.map { heartRateWithMetadata -> heartRateWithMetadata.asHeartRate() as T }
        }

    override suspend fun <T : Measurement> deleteMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val value = measurement as HeartRate

        val heartRateWithMetadata = value.asHeartRateWithMetadata()

        heartRateDao.insert(
            heartRateWithMetadata.copy(
                heartRateEntity = heartRateWithMetadata.heartRateEntity.copy(isRemoved = true)
            )
        )
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while deleteMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurement(
        measurement: T
    ): Result<T> = runCatching {
        val heartRate = measurement as HeartRate

        heartRateDao.insert(heartRate.asHeartRateWithMetadata())
        measurement
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurement running")
    }

    override suspend fun <T : Measurement> writeMeasurements(
        measurements: List<T>
    ): Result<List<T>> = runCatching {
        val heartRates = measurements as List<HeartRate>

        logV("writeMeasurements called: measurements=$heartRates")
        val d = heartRates.map { heartRate -> heartRate.asHeartRateWithMetadata() }
        logV("writeMeasurements called: HeartRatesWithMetadata=$d")
        heartRateDao.insertAllHeartRatesWithMetadata(heartRatesWithMetadata = d)
        measurements
    }.onFailure { exception ->
        logE(exception, "Error while writeMeasurements running")
    }
}
