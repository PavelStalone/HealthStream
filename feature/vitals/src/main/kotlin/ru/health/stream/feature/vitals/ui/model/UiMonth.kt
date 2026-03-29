package ru.health.stream.feature.vitals.ui.model

import kotlinx.datetime.format.MonthNames

internal val MonthNames.Companion.RUSSIAN_FULL: MonthNames
    get() = MonthNames(
        listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
    )
