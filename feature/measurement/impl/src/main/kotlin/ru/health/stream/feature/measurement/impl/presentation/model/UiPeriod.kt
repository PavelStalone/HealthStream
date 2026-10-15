package ru.health.stream.feature.measurement.impl.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.R
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.data.vitals.model.Period

@Immutable
internal sealed class UiPeriod(
    val label: UiText,
) {

    data object Today : UiPeriod(
        label = UiText.Translatable(R.string.period_today)
    )

    data object Week : UiPeriod(
        label = UiText.Translatable(R.string.period_week)
    )

    data object Month : UiPeriod(
        label = UiText.Translatable(R.string.period_month)
    )

    data object Year : UiPeriod(
        label = UiText.Translatable(R.string.period_year)
    )
}

internal fun UiPeriod.asPeriod(firstDayOfWeek: DayOfWeek): Period = when (this) {
    UiPeriod.Today -> Period.Day
    UiPeriod.Week -> Period.Week(firstDayOfWeek = firstDayOfWeek)
    UiPeriod.Month -> Period.Month
    UiPeriod.Year -> Period.Year
}
