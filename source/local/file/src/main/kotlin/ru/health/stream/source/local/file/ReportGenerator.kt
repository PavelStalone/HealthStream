package ru.health.stream.source.local.file

import kotlinx.datetime.Instant
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.vitals.model.measurement.Measurement
import java.io.File

internal interface ReportGenerator {

    suspend fun generateFile(
        user: User?,
        outputFile: File,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>,
    ): File
}
