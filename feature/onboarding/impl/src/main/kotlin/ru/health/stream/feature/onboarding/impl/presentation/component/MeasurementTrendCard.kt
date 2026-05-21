package ru.health.stream.feature.onboarding.impl.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.composition.LocalLocale
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.core.chart.api.LineChart
import ru.health.stream.core.chart.core.Drawable
import ru.health.stream.core.chart.core.YAxisSide
import ru.health.stream.core.chart.core.drawable.GridLines

@Composable
internal fun MeasurementTrendCard(
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    animation: Boolean = true,
    chartDrawables: List<Drawable> = emptyList(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    period: Period = Period.Week(firstDayOfWeek = firstDayOfWeek),
) {
    val locale = LocalLocale.current
    val timeZone = LocalTimeZone.current

    val display = remember(locale, period, timeZone, firstDayOfWeek) {
        when (period) {
            Period.Day -> Period.Day.getDisplay()
            Period.Month -> Period.Month.getDisplay(
                locale = locale,
                timeZone = timeZone,
                date = Clock.System.now(),
                firstDayOfWeek = firstDayOfWeek,
            )

            is Period.Week -> period.getDisplay(locale = locale)
            Period.Year -> Period.Year.getDisplay(locale = locale)
            else -> emptyMap()
        }
    }

    val yLabels = remember(yRange) {
        val step = 10f
        val start = yRange.start
        val end = yRange.endInclusive

        generateSequence(seed = start) { it + step }
            .takeWhile { it <= end }
            .toList()
    }

    LineChart(
        modifier = modifier,
        animation = animation,
        xRange = 0f..1f,
        yRange = yRange,
        chartDrawables = buildList {
            add(
                GridLines(
                    values = yLabels,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
            addAll(elements = chartDrawables)
        }
    ) {
        display.forEach { (x, text) ->
            Text(
                modifier = Modifier
                    .bindXAxis(
                        x = x,
                        alignment = when (x) {
                            0f -> Alignment.End
                            1f -> Alignment.Start
                            else -> Alignment.CenterHorizontally
                        }
                    )
                    .padding(top = 16.dp),
                text = text,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        yLabels.forEach { y ->
            Text(
                modifier = Modifier
                    .bindYAxis(y = y, side = YAxisSide.Left)
                    .padding(end = 8.dp),
                text = y.toInt().toString(),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
