package ru.health.stream.source.local.file.model

enum class MeasurementType(
    val text: String,
) {

    BODY_WEIGHT(text = "Вес тела"),
    BLOOD_GLUCOSE(text = "Глюкоза в крови"),
    RESPIRATION_RATE(text = "Частота дыхания"),
    OXYGEN_SATURATION(text = "Сатурация (SpO2)"),
    BLOOD_PRESSURE(text = "Артериальное давление"),
    HEART_RATE(text = "Частота сердечных сокращений"),
    ;
}
