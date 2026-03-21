package ru.health.stream.feature.vitals.data.model.measurement

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.data.model.EmptyMetadata
import ru.health.stream.feature.vitals.data.model.Metadata
import ru.health.stream.feature.vitals.data.model.Resource
import kotlin.uuid.Uuid

interface SystolicPressure : HealthMeasurement {

    val systolic: Float
}

interface DiastolicPressure : HealthMeasurement {

    val diastolic: Float
}

data class BloodPressure(
    override val id: String,
    override val systolic: Float,
    override val diastolic: Float,
    override val createdAt: Instant,
    override val resource: Resource,
    override val metadata: Metadata = EmptyMetadata,
) : HealthMeasurement, SystolicPressure, DiastolicPressure, Metadata by metadata {

    init {
        Uuid.parse(id) // Check uuid format
        require(systolic > 0) { "Systolic pressure must be positive: $systolic" }
        require(diastolic > 0) { "Diastolic pressure must be positive: $diastolic" }
        require(systolic > diastolic) { "Systolic pressure ($systolic) must be greater than diastolic ($diastolic)" }
    }
}
