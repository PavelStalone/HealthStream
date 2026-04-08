package ru.health.stream.source.local.room.table

import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.dao.HeartRateDao
import ru.health.stream.source.local.room.dao.MeasurementDao
import ru.health.stream.source.local.room.entity.HeartRateEntity
import ru.health.stream.source.local.room.mapper.asHeartRate
import ru.health.stream.source.local.room.mapper.asHeartRateEntity
import javax.inject.Inject
import kotlin.reflect.KClass

internal class HeartRateTable @Inject constructor(
    private val heartRateDao: HeartRateDao,
) : MeasurementDao<HeartRateEntity> by heartRateDao,
    MeasurementTable<HeartRate, HeartRateEntity>() {

    override val type: KClass<HeartRate> = HeartRate::class

    override fun mapToMeasurement(entity: Any?): HeartRate =
        (entity as HeartRateEntity).asHeartRate()

    override fun mapToEntity(measurement: HeartRate): HeartRateEntity =
        measurement.asHeartRateEntity()
}
