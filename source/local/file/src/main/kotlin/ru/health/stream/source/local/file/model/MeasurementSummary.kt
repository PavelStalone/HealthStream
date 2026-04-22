package ru.health.stream.source.local.file.model

internal data class MeasurementSummary(
    val counts: Int,
    val section: MeasurementSection,
    val estimationsCount: Map<out ReportEstimation, Int>,
)
