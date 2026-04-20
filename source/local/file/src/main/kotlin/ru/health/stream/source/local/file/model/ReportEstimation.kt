package ru.health.stream.source.local.file.model

import com.itextpdf.kernel.colors.DeviceRgb

enum class ReportEstimation(
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
