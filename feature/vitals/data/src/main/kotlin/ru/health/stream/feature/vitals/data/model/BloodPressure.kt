package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant

interface SystolicPressure : HealthMeasurement {

    val systolic: Float
}

interface DiastolicPressure : HealthMeasurement {

    val diastolic: Float
}

sealed interface BloodPressure : HealthMeasurement, SystolicPressure, DiastolicPressure {

    data class Simple(
        override val id: String,
        override val systolic: Float,
        override val diastolic: Float,
        override val createdAt: Instant,
    ) : BloodPressure

    data class WithResource(
        override val resource: Resource,
        private val bloodPressure: BloodPressure,
    ) : BloodPressure by bloodPressure, HealthMeasurement.WithResource
}
