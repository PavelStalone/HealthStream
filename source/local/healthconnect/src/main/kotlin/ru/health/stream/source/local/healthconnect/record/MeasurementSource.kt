package ru.health.stream.source.local.healthconnect.record

import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

internal abstract class MeasurementSource<T : Measurement>() : MeasurementSourceContract<T> {

    abstract val type: KClass<T>
}
