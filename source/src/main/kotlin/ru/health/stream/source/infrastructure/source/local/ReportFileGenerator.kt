package ru.health.stream.source.infrastructure.source.local

import kotlinx.datetime.Instant
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.data.vitals.model.measurement.Measurement
import java.net.URI

interface ReportFileGenerator {

    suspend fun generateFile(
        user: User?,
        format: ReportFormat,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>,
    ): URI
}
