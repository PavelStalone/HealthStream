package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource

/**
 * Measurements with main information
 */
sealed interface Measurement : Metadata {

    val id: String
    val createdAt: Instant
    val resource: Resource
    val metadata: Metadata
}

fun Measurement.copy(
    id: String = this.id,
    metadata: Metadata = this.metadata,
): Measurement = when (this) {
    is HeartRate -> copy(id = id, metadata = metadata)
    is BodyWeight -> copy(id = id, metadata = metadata)
    is BloodGlucose -> copy(id = id, metadata = metadata)
    is BloodPressure -> copy(id = id, metadata = metadata)
    is RespirationRate -> copy(id = id, metadata = metadata)
    is OxygenSaturation -> copy(id = id, metadata = metadata)
    else -> this
}
