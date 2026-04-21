package ru.health.stream.data.vitals.domain.estimation

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.Measurement
import kotlin.reflect.KClass

interface MeasurementAssessor<T : Measurement> {

    val type: KClass<T>

    suspend fun analyze(measurement: T): Estimation?
    suspend fun levels(date: Instant): Map<Estimation.Level, List<ClosedRange<Float>>>
}
