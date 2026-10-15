package ru.health.stream.core.ui.model

import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import java.util.Locale

val MonthNames.Companion.RUSSIAN_FULL: MonthNames
    get() = MonthNames(
        listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
    )

fun MonthNames.Companion.localized(locale: Locale): MonthNames =
    if (locale.language == "ru") {
        MonthNames.RUSSIAN_FULL
    } else {
        MonthNames.ENGLISH_FULL
    }

fun DayOfWeekNames.Companion.localized(locale: Locale): DayOfWeekNames =
    if (locale.language == "ru") {
        DayOfWeekNames.RUSSIAN_ABBREVIATED
    } else {
        DayOfWeekNames.ENGLISH_ABBREVIATED
    }

val DayOfWeekNames.Companion.RUSSIAN_ABBREVIATED: DayOfWeekNames
    get() = DayOfWeekNames(
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    )
