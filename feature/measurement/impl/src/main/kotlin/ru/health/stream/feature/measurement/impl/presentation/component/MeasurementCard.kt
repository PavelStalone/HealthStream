package ru.health.stream.feature.measurement.impl.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.composition.LocalLocale
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.layout.RowByFirstBaseLine
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.feature.chart.api.LineChart
import ru.health.stream.feature.chart.core.ChartScope
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.drawable.CubicArea
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.model.ChartPosition
import java.lang.Math.random

@Composable
internal fun MeasurementCard(
    onClick: () -> Unit,
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

    Card(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        measurementIcon.drawIcon(
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = measurementTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
                Text(
                    modifier = Modifier
                        .background(
                            color = Color(color = 0xFFE8F5E9),
                            shape = CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    text = "Норма",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(color = 0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            if (measurementValue != null) {
                RowByFirstBaseLine(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = measurementValue,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = measurementUnit,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }

                if (chartDrawables.isNotEmpty()) {
                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
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
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                ),
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных за этот период",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMeasurementCards() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val points = List(size = 8) { index ->
                ChartPosition.Point(
                    x = index.toFloat() / 7f,
                    y = 60 + random().toFloat() * 30,
                )
            }

            MeasurementCard(
                modifier = Modifier.fillMaxWidth(),
                measurementUnit = "уд/мин",
                measurementTitle = "Пульс",
                onClick = {},
                measurementIcon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
                measurementValue = points.last().y.toInt().toString(),
                yRange = 40f..120f,
                chartDrawables = listOf(
                    CubicArea(
                        points = points,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                            )
                        )
                    ),
                    CubicLine(
                        points = points,
                        style = Stroke(width = 8f),
                        color = MaterialTheme.colorScheme.primary,
                    )
                )
            )

            MeasurementCard(
                modifier = Modifier.fillMaxWidth(),
                measurementUnit = "кг",
                measurementTitle = "Вес тела",
                onClick = {},
                measurementIcon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
                measurementValue = null,
                yRange = 40f..90f,
                chartDrawables = emptyList()
            )
        }
    }
}
