package ru.health.stream.source.local.room

import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.PrimaryMeasurementSource
import kotlin.reflect.KClass

internal abstract class MeasurementTable<T : Measurement> : PrimaryMeasurementSource {

    abstract val type: KClass<T>
}
