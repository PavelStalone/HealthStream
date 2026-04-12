package ru.health.stream.data.vitals.domain.estimation

import jakarta.inject.Singleton
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.measurement.Measurement
import javax.inject.Inject

@Singleton
class MeasurementAnalyzer @Inject constructor(
    private val measurementAssessors: List<@JvmSuppressWildcards MeasurementAssessor<Measurement>>
) {

    suspend fun analyze(measurement: Measurement): Estimation? = measurementAssessors
        .firstOrNull { assessor -> assessor.type == measurement::class }
        ?.analyze(measurement)
}
