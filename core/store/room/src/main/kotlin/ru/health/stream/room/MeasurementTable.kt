package ru.health.stream.room

import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.room.dao.MeasurementDao
import kotlin.reflect.KClass

internal abstract class MeasurementTable<T : HealthMeasurement, E>() : MeasurementDao<E> {

    abstract val kClass: KClass<T>

    abstract fun mapToMeasurement(entity: Any?): T
    abstract fun mapToEntity(measurement: T): E

    suspend fun insert(measurement: T) = insert(mapToEntity(measurement))
}
