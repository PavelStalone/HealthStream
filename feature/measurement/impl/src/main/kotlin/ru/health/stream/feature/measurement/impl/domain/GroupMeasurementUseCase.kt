package ru.health.stream.feature.measurement.impl.domain

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.DiastolicPressure
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.model.measurement.SystolicPressure

class GroupMeasurementUseCase {

    data class GroupResult<T : Measurement>(
        val min: T,
        val max: T,
        val items: List<T>,
    )

    fun <T : Measurement> invoke(
        measurements: List<T>,
        groupSelector: (T) -> Instant,
        comparator: Comparator<T> = compareBy { measurement ->
            when (measurement) {
                is HeartRate -> measurement.pulse
                is BloodGlucose -> measurement.level
                is RespirationRate -> measurement.rate
                is BodyWeight -> measurement.weight.kg
                is SystolicPressure -> measurement.systolic
                is DiastolicPressure -> measurement.diastolic
                is OxygenSaturation -> measurement.saturation
            }
        },
    ): Map<Instant, GroupResult<T>> {
        if (measurements.isEmpty()) return emptyMap()

        val grouped = measurements.groupBy(groupSelector)

        return grouped.mapValues { (_, items) ->
            GroupResult(
                min = items.minWith(comparator),
                max = items.maxWith(comparator),
                items = items
            )
        }.toSortedMap()
    }
}
