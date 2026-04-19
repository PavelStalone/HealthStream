package ru.health.stream.data.report.repository.impl

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.report.api.ReportFileGenerator
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.data.report.repository.ReportRepository
import ru.health.stream.data.vitals.model.measurement.Measurement
import java.io.File
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val fileGenerator: ReportFileGenerator,
) : ReportRepository {

    override suspend fun generateReport(
        format: ReportFormat,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>,
    ): File {
        val user = userRepository.getUser()

        return fileGenerator.generateFile(
            user = user,
            format = format,
            dateRange = dateRange,
            measurements = measurements,
        )
    }
}
