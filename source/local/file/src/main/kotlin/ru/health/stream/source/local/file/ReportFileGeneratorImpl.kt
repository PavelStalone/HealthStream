package ru.health.stream.source.local.file

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.report.api.ReportFileGenerator
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.file.pdf.PdfReportGenerator
import java.io.File


internal class ReportFileGeneratorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReportFileGenerator {

    private val pdfGenerator: ReportGenerator = PdfReportGenerator(context)

    override suspend fun generateFile(
        user: User?,
        format: ReportFormat,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>
    ): File {
        logV("PdfGenerator called: user: $user, format: $format, measurements: ${measurements.size}, dateRange: $dateRange")

        val file = File(context.cacheDir, "report.pdf")

        pdfGenerator.generateFile(
            user = user,
            outputFile = file,
            dateRange = dateRange,
            measurements = measurements,
        )

        logV("PdfGenerator finish")

        return file
    }
}
