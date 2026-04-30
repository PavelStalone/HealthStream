package ru.health.stream.data.report.model

import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.MeasurementGroup

data class MeasurementSummary(
    val counts: Int,
    val group: MeasurementGroup,
    val estimationsCount: Map<out Estimation.Level, Int>,
)
