package ru.health.stream.feature.vitals.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.composition.LocalLocale
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.core.ui.layout.RowByFirstBaseLine
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.feature.chart.api.LineChart
import ru.health.stream.feature.chart.core.ChartScope
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.vitals.data.model.Period
import java.lang.Math.random

@Composable
fun MeasurementCard(
    measurementUnit: String,
    measurementValue: String?,
    measurementTitle: String,
    measurementIcon: UiIcon,
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    animation: Boolean = true,
    chartDrawables: List<Drawable> = emptyList(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    period: Period = Period.Week(firstDayOfWeek = firstDayOfWeek),
    chartContent: @Composable ChartScope.() -> Unit = {},
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
        }
    }

    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                measurementIcon.drawIcon(
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = measurementTitle,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // TODO: Change to separate component - shoplikpavel 2026-02-04
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 4.dp),
                text = "Normal",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall,
            )
        }

        if (chartDrawables.isNotEmpty() && measurementValue != null) {
            RowByFirstBaseLine(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = measurementValue,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = measurementUnit,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                animation = animation,
                xRange = 0f..1f,
                yRange = yRange,
                chartDrawables = chartDrawables,
            ) {
                chartContent()
                display.forEach { (x, text) ->
                    Text(
                        modifier = Modifier.bindXAxis(
                            x = x,
                            alignment = when (x) {
                                0f -> Alignment.End
                                1f -> Alignment.Start
                                else -> Alignment.CenterHorizontally
                            }
                        ),
                        text = text,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    text = "Please take measurements",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMeasurementCards() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(2) {
                val points = List(7) { index ->
                    ChartPosition.Point(
                        x = index.toFloat() / 6f,
                        y = 40 + random().toFloat() * 50,
                        z = 0f
                    )
                }

                MeasurementCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 8.dp),
                    measurementUnit = "bpm",
                    measurementTitle = "Pulse",
                    measurementIcon = UiIcon.Vector(Icons.Rounded.FavoriteBorder),
                    measurementValue = points.maxOf { it.y.toInt() }.toString(),
                    yRange = 40f..90f,
                    chartDrawables = listOf(
                        CubicLine(
                            points = points,
                            style = Stroke(width = 16f),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    )
                )
            }

            MeasurementCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 8.dp),
                measurementUnit = "bpm",
                measurementTitle = "Pulse",
                measurementIcon = UiIcon.Vector(Icons.Rounded.FavoriteBorder),
                measurementValue = null,
                yRange = 40f..90f,
                chartDrawables = emptyList()
            )
        }
    }
}
