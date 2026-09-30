package ru.health.stream.source.local.healthconnect.record

import ru.health.stream.core.common.permission.Permission
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

internal abstract class MeasurementSource<T : Measurement> : MeasurementSourceContract<T> {

    abstract val type: KClass<T>
    abstract val readPermission: Permission
    abstract val writePermission: Permission
}
