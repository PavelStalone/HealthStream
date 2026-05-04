package ru.health.stream.feature.measurement.impl.presentation.component

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.composition.LocalLocale
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.feature.chart.api.LineChart
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.YAxisSide
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.GridLines
import ru.health.stream.feature.chart.model.ChartPosition

@Composable
internal fun MeasurementTrendCard(
    period: Period,
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    animation: Boolean = true,
    chartDrawables: List<Drawable> = emptyList(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
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

@Preview
@Composable
private fun PreviewMeasurementTrendCard() {
    MaterialTheme {
        val points = listOf(
            ChartPosition.Point(0 / 6f, 80f),
            ChartPosition.Point(1 / 6f, 92f),
            ChartPosition.Point(2 / 6f, 58f),
            ChartPosition.Point(3 / 6f, 78f),
            ChartPosition.Point(4 / 6f, 70f),
            ChartPosition.Point(5 / 6f, 68f),
            ChartPosition.Point(6 / 6f, 82f),
        )

        MeasurementTrendCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp),
            yRange = 50f..100f,
            chartDrawables = listOf(
                CubicLine(
                    points = points,
                    style = Stroke(width = 16f),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            ),
            firstDayOfWeek = DayOfWeek.MONDAY,
            period = Period.Week(firstDayOfWeek = DayOfWeek.MONDAY)
        )
    }
}
