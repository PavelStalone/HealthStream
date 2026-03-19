package ru.health.stream.core.store.healthconnect.record

import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import kotlin.reflect.KClass

internal abstract class MeasurementSource<T : HealthMeasurement>() : MeasurementSourceContract<T> {

    abstract val type: KClass<T>
}
