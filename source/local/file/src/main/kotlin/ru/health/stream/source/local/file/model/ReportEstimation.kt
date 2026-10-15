package ru.health.stream.source.local.file.model

import android.content.Context
import com.itextpdf.kernel.colors.DeviceRgb
import ru.health.stream.core.ui.R
import ru.health.stream.data.vitals.model.Estimation

internal enum class ReportEstimation(
    val color: DeviceRgb,
    val backgroundColor: DeviceRgb? = null,
) {

    LOW(
        color = ACCENT,
        backgroundColor = LOW_LEVEL_CONTAINER,
    ),
    NORMAL(
        color = GREEN,
    ),
    HIGH(
        color = YELLOW,
        backgroundColor = HIGH_LEVEL_CONTAINER,
    ),
    CRITICAL(
        color = RED,
        backgroundColor = EXTRA_HIGH_LEVEL_CONTAINER,
    ),
    ;

    fun localizedText(context: Context): String = when (this) {
        LOW -> context.getString(R.string.report_estimation_low)
        NORMAL -> context.getString(R.string.report_estimation_normal)
        HIGH -> context.getString(R.string.report_estimation_high)
        CRITICAL -> context.getString(R.string.report_estimation_critical)
    }
}

internal fun Estimation.Level.asReportEstimation(): ReportEstimation = when (this) {
    Estimation.Level.LOW -> ReportEstimation.LOW
    Estimation.Level.NORMAL -> ReportEstimation.NORMAL
    Estimation.Level.HIGH -> ReportEstimation.HIGH
    Estimation.Level.CRITICAL -> ReportEstimation.CRITICAL
}
