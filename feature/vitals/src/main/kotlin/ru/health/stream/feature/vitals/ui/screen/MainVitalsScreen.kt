package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.health.stream.core.ui.model.asText
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.vitals.ui.component.MainWeekCard

@Composable
internal fun MainVitalsScreen() {
    val vitalsViewModel: MainVitalsViewModel = hiltViewModel()

    val weekCards by vitalsViewModel.weekCardStates.collectAsStateWithLifecycle(initialValue = emptyList())

    if (weekCards.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = weekCards,
                key = WeekCardState::key,
            ) { weekCard ->
                with(weekCard) {
                    MainWeekCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(horizontal = 8.dp),
                        measurementIcon = measurementIcon,
                        measurementUnit = measurementUnit.asText(),
                        measurementTitle = measurementUnit.asText(),
                        measurementValue = measurementValue?.asText(),
                        yRange = if (points.isNotEmpty()) {
                            points.minOf { point -> point.y }..points.maxOf { point -> point.y }
                        } else {
                            0f..1f
                        },
                        chartDrawables = if (points.isNotEmpty()) {
                            listOf(
                                CubicLine(
                                    points = points,
                                    style = Stroke(width = 16f),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            )
                        } else {
                            emptyList()
                        }
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Not found measurements")
        }
    }
}
