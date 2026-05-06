package ru.health.stream.source.local.file

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.common.di.Dispatcher
import ru.health.stream.core.monitor.logV
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.data.report.usecase.CalculateMeasurementSummaryUseCase
import ru.health.stream.data.vitals.domain.estimation.MeasurementAnalyzer
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.usecase.GroupMeasurementByPeriodUseCase
import ru.health.stream.source.infrastructure.source.local.ReportFileGenerator
import ru.health.stream.source.local.file.pdf.PdfReportGenerator
import java.net.URI
import javax.inject.Inject

internal class ReportFileGeneratorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val measurementAnalyzer: MeasurementAnalyzer,
    @Dispatcher(Dispatcher.IO) val ioDispatcher: CoroutineDispatcher,
    private val groupMeasurementByPeriodUseCase: GroupMeasurementByPeriodUseCase,
    private val calculateMeasurementSummaryUseCase: CalculateMeasurementSummaryUseCase,
) : ReportFileGenerator {

    private val pdfGenerator: ReportGenerator = PdfReportGenerator(
        context = context,
        measurementAnalyzer = measurementAnalyzer,
        groupMeasurementByPeriodUseCase = groupMeasurementByPeriodUseCase,
        calculateMeasurementSummaryUseCase = calculateMeasurementSummaryUseCase,
    )
    private val localDateTimeFormatter = LocalDateTime.Format {
        dayOfMonth()
        char('.')
        monthNumber()
        char('.')
        year()
        char('_')
        hour()
        char('-')
        minute()
        char('-')
        second()
    }

    override suspend fun generateFile(
        user: User?,
        format: ReportFormat,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>
    ): URI {
        logV("generateFile called: user: $user, format: $format, measurements: ${measurements.size}, dateRange: $dateRange")

        return withContext(ioDispatcher) {
            val fileName = createFileName(format)
            val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, format.toMimeType())
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOCUMENTS}/HealthStreamApp"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(contentUri, contentValues)
                ?: throw IllegalStateException("Failed to create MediaStore entry")

            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    when (format) {
                        ReportFormat.PDF -> pdfGenerator.generate(
                            user = user,
                            dateRange = dateRange,
                            outputStream = outputStream,
                            measurements = measurements,
                        )

                        ReportFormat.CSV -> {
                            // TODO: Implement CSV generator if needed
                        }
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                logV("PdfGenerator finish: $uri")
                URI(uri.toString())
            }.onFailure {
                context.contentResolver.delete(uri, null, null)
            }.getOrThrow()
        }
    }

    private fun createFileName(format: ReportFormat): String {
        val timestamp = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(localDateTimeFormatter)
        val extension = when (format) {
            ReportFormat.PDF -> "pdf"
            ReportFormat.CSV -> "csv"
        }

        return "Report_$timestamp.$extension"
    }

    private fun ReportFormat.toMimeType(): String = when (this) {
        ReportFormat.PDF -> "application/pdf"
        ReportFormat.CSV -> "text/csv"
    }
}
