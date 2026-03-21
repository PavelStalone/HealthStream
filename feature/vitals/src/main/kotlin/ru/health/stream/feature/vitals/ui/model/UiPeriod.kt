package ru.health.stream.feature.vitals.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.vitals.data.model.Period

@Immutable
internal sealed class UiPeriod(
    val label: UiText,
) {

    data object Today : UiPeriod(
        label = UiText.NonTranslatable("Today")
    )

    data object Week : UiPeriod(
        label = UiText.NonTranslatable("Week")
    )

    data object Month : UiPeriod(
        label = UiText.NonTranslatable("Month")
    )

    data object Year : UiPeriod(
        label = UiText.NonTranslatable("Year")
    )
}

internal fun UiPeriod.asPeriod(firstDayOfWeek: DayOfWeek): Period = when (this) {
    UiPeriod.Today -> Period.Day
    UiPeriod.Week -> Period.Week(firstDayOfWeek = firstDayOfWeek)
    UiPeriod.Month -> Period.Month
    UiPeriod.Year -> Period.Year
}
