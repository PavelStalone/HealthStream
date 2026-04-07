package ru.health.stream.source.local.room

import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.room.dao.MeasurementDao
import kotlin.reflect.KClass

internal abstract class MeasurementTable<T : Measurement, E>() : MeasurementDao<E> {

    abstract val type: KClass<T>

    abstract fun mapToMeasurement(entity: Any?): T
    abstract fun mapToEntity(measurement: T): E

    suspend fun insert(measurement: T) = insert(mapToEntity(measurement))
}
