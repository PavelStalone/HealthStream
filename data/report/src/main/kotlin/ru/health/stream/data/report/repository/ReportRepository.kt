package ru.health.stream.data.report.repository

import kotlinx.datetime.Instant
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.data.vitals.model.measurement.Measurement
import java.io.File

interface ReportRepository {
    suspend fun generateReport(
        format: ReportFormat,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>,
    ): File
}
