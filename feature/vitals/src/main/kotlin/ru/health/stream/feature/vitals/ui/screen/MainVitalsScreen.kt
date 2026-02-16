package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.vitals.ui.component.MainWeekCard
import java.lang.Math.random

@Composable
internal fun MainVitalsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(10) {
            val points = List(7) { index ->
                ChartPosition.Point(
                    x = index.toFloat(),
                    y = 40 + random().toFloat() * 50,
                    z = 0f
                )
            }
            item {
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
