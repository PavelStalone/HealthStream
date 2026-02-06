package ru.health.stream.feature.vitals.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.health.stream.feature.chart.api.LineChart
import ru.health.stream.feature.chart.core.CubicLine
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.model.ChartPosition

@Composable
fun MainCard(
    measurementUnit: String,
    measurementValue: String,
    measurementTitle: String,
    animation: Boolean = true,
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    chartDrawables: List<Drawable> = emptyList(),
) {
    Card(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.CenterStart)) {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Rounded.FavoriteBorder,
                )
                Text(text = measurementTitle)
            }

            // TODO: Change to separate component - shoplikpavel 2026-02-04
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(color = Color.Cyan, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp),
                text = "Normal"
            )
        }

        Row {
            Text(text = measurementValue)
            Text(text = measurementUnit)
        }

        LineChart(
            modifier = Modifier.fillMaxWidth(),
            animation = animation,
            xRange = 0f..6f,
            yRange = yRange,
            chartDrawables = chartDrawables,
        ) {
            Text(
                modifier = Modifier.bindValue(xValue = 0f, alignment = Alignment.CenterEnd),
                text = "Su"
            )
        }
    }
}

@Preview
@Composable
fun PreviewMainCard() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Gray)
        ) {
            val transition = rememberInfiniteTransition()
            val anim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    tween(1000), RepeatMode.Reverse
                )
            )

            MainCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                measurementUnit = "bpm",
                measurementTitle = "Pulse",
                measurementValue = "72",
                yRange = 40f..90f,
                chartDrawables = listOf(
                    CubicLine(
                        points = listOf(
                            ChartPosition.Point(x = 0f, y = 40f, z = 0f),
                            ChartPosition.Point(x = 1f, y = 80f, z = 0f),
                            ChartPosition.Point(x = 2f, y = 65f, z = 0f),
                            ChartPosition.Point(x = 5f, y = 85f, z = 0f),
                        )
                    )
                )
            )
        }
    }
}
