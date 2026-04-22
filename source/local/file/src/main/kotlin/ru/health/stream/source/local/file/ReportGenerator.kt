package ru.health.stream.source.local.file

import kotlinx.datetime.Instant
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.vitals.model.measurement.Measurement
import java.io.OutputStream

internal interface ReportGenerator {

    suspend fun generate(
        user: User?,
        outputStream: OutputStream,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>,
    )
}
