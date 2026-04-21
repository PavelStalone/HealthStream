package ru.health.stream.data.vitals.domain.estimation

import jakarta.inject.Singleton
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.Measurement
import javax.inject.Inject
import kotlin.reflect.KClass

@Singleton
class MeasurementAnalyzer @Inject constructor(
    private val measurementAssessors: List<@JvmSuppressWildcards MeasurementAssessor<Measurement>>
) {

    suspend fun levels(
        date: Instant,
        type: KClass<out Measurement>,
    ): Map<Estimation.Level, List<ClosedRange<Float>>> = measurementAssessors
        .firstOrNull { assessor -> assessor.type == type }
        ?.levels(date = date)
        ?: emptyMap()

    suspend fun analyze(measurement: Measurement): Estimation? = measurementAssessors
        .firstOrNull { assessor -> assessor.type == measurement::class }
        ?.analyze(measurement)
}
