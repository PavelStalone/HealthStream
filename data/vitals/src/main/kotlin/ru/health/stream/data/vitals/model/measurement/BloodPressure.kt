package ru.health.stream.data.vitals.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Metadata
import ru.health.stream.data.vitals.model.Resource

interface SystolicPressure : Measurement {

    val systolic: Float
}

interface DiastolicPressure : Measurement {

    val diastolic: Float
}

data class BloodPressure(
    override val id: String,
    override val systolic: Float,
    override val diastolic: Float,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
) : Measurement, SystolicPressure, DiastolicPressure, Metadata by metadata

internal fun BloodPressure.check() {
    require(systolic > 0) { "Systolic pressure must be positive: $systolic" }
    require(diastolic > 0) { "Diastolic pressure must be positive: $diastolic" }
    require(systolic > diastolic) { "Systolic pressure ($systolic) must be greater than diastolic ($diastolic)" }
}
