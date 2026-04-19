package ru.health.stream.source.local.file.model

data class MeasurementSummary(
    val counts: Int,
    val section: MeasurementSection,
    val estimationsCount: Map<out ReportEstimation, Int>,
)
