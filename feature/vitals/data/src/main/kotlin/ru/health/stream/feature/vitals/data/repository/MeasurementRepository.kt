package ru.health.stream.feature.vitals.data.repository

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.VitalMeasurement
import kotlin.reflect.KClass
import kotlin.time.Duration

interface MeasurementRepository {

    fun <T: VitalMeasurement> getMeasurementsByRange(from: Instant, to: Instant, type: KClass<T>): List<T>
    fun <T: VitalMeasurement> getMeasurementsByDuration(duration: Duration, type: KClass<T>): List<T>
    fun <T: VitalMeasurement> createMeasurement(measurement: T): Result<T>
}
