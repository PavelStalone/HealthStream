package ru.health.stream.feature.measurement.impl.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.data.vitals.model.Period

@Immutable
internal sealed class UiPeriod(
    val label: UiText,
) {

    data object Today : UiPeriod(
        label = UiText.NonTranslatable(value = "Сегодня")
    )

    data object Week : UiPeriod(
        label = UiText.NonTranslatable(value = "Неделя")
    )

    data object Month : UiPeriod(
        label = UiText.NonTranslatable(value = "Месяц")
    )

    data object Year : UiPeriod(
        label = UiText.NonTranslatable(value = "Год")
    )
}

internal fun UiPeriod.asPeriod(firstDayOfWeek: DayOfWeek): Period = when (this) {
    UiPeriod.Today -> Period.Day
    UiPeriod.Week -> Period.Week(firstDayOfWeek = firstDayOfWeek)
    UiPeriod.Month -> Period.Month
    UiPeriod.Year -> Period.Year
}
