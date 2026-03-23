package ru.health.stream.feature.vitals.domain

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.Period
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement

class GroupMeasurementUseCase(
    val measurements: List<HealthMeasurement>,
    val period: Period,
) {

    operator fun invoke(): Map<Instant, HealthMeasurement> {
        val selector: (Instant) -> Instant = when (period) {
            Period.Day -> { instant -> instant }
            Period.Month -> TODO()
            is Period.Week -> TODO()
            Period.Year -> TODO()
        }
        measurements.groupBy { }

        TODO()
    }
}
