package ru.health.stream.data.report.model

enum class ReportFormat(val enabled: Boolean = true) {

    PDF,
    CSV(false),
    ;
}
