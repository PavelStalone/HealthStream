package ru.health.stream.source.local.file.model

import com.itextpdf.kernel.colors.DeviceRgb
import ru.health.stream.data.vitals.model.Estimation

internal enum class ReportEstimation(
    val text: String,
    val color: DeviceRgb,
    val backgroundColor: DeviceRgb? = null,
) {

    LOW(
        color = ACCENT,
        text = "Понижено",
        backgroundColor = DeviceRgb(0xFD, 0xE8, 0xE8),
    ),
    NORMAL(
        color = GREEN,
        text = "Нормально",
    ),
    HIGH(
        color = YELLOW,
        text = "Повышено",
        backgroundColor = DeviceRgb(0xFE, 0xF7, 0xE0),
    ),
    EXTRA_HIGH(
        color = RED,
        text = "Критично",
        backgroundColor = DeviceRgb(0xFD, 0xE8, 0xE8),
    ),
    ;
}

internal fun Estimation.Level.asReportEstimation(): ReportEstimation = when (this) {
    Estimation.Level.LOW -> ReportEstimation.LOW
    Estimation.Level.NORMAL -> ReportEstimation.NORMAL
    Estimation.Level.HIGH -> ReportEstimation.HIGH
    Estimation.Level.EXTRA_HIGH -> ReportEstimation.EXTRA_HIGH
}
