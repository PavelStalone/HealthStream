package ru.health.stream.room.table

import ru.health.stream.feature.vitals.data.model.HeartRate
import ru.health.stream.room.MeasurementTable
import ru.health.stream.room.dao.HeartRateDao
import ru.health.stream.room.dao.MeasurementDao
import ru.health.stream.room.entity.HeartRateEntity
import ru.health.stream.room.mapper.asHeartRate
import ru.health.stream.room.mapper.asHeartRateEntity
import javax.inject.Inject
import kotlin.reflect.KClass

internal class HeartRateTable @Inject constructor(
    private val heartRateDao: HeartRateDao,
) : MeasurementDao<HeartRateEntity> by heartRateDao,
    MeasurementTable<HeartRate.WithResource, HeartRateEntity>() {

    override val type: KClass<HeartRate.WithResource> = HeartRate.WithResource::class

    override fun mapToMeasurement(entity: Any?): HeartRate.WithResource =
        (entity as HeartRateEntity).asHeartRate()

    override fun mapToEntity(measurement: HeartRate.WithResource): HeartRateEntity =
        measurement.asHeartRateEntity()
}
