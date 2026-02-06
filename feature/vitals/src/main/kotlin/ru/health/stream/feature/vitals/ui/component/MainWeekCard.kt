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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.layout.RowByFirstBaseLine
import ru.health.stream.feature.chart.api.LineChart
import ru.health.stream.feature.chart.core.ChartScope
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.model.ChartPosition
import java.lang.Math.random

@Composable
fun MainWeekCard(
    measurementUnit: String,
    measurementValue: String,
    measurementTitle: String,
    measurementIcon: ImageVector,
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    animation: Boolean = true,
    chartDrawables: List<Drawable> = emptyList(),
    chartContent: @Composable ChartScope.() -> Unit = {},
) {
    // TODO: Change this on string resources - shoplikpavel 2026-02-06
    val week = remember { listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa") }

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
                Icon(
                    contentDescription = null,
                    imageVector = measurementIcon,
                    tint = MaterialTheme.colorScheme.primary,
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
            xRange = 0f..6f,
            yRange = yRange,
            chartDrawables = chartDrawables,
        ) {
            chartContent()
            week.forEachIndexed { index, day ->
                Text(
                    modifier = Modifier.bindXAxis(
                        x = index.toFloat(),
                        alignment = when (index) {
                            0 -> Alignment.End
                            week.size - 1 -> Alignment.Start
                            else -> Alignment.CenterHorizontally
                        }
                    ),
                    text = day,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMainWeekCards() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(2) {
                val points = List(7) { index ->
                    ChartPosition.Point(
                        x = index.toFloat(),
                        y = 40 + random().toFloat() * 50,
                        z = 0f
                    )
                }

                MainWeekCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 8.dp),
                    measurementUnit = "bpm",
                    measurementTitle = "Pulse",
                    measurementIcon = Icons.Rounded.FavoriteBorder,
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
        }
    }
}
