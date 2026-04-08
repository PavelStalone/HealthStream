package ru.health.stream.feature.home.impl.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.health.stream.core.ui.model.asText
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.feature.chart.core.drawable.CubicArea
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.home.impl.presentation.viewmodel.HomeViewModel
import ru.health.stream.feature.home.impl.presentation.viewmodel.WeekCardState
import ru.health.stream.feature.home.impl.presentation.component.MeasurementCard
import kotlin.reflect.KClass

@Composable
internal fun HomeScreen(
    onMeasurementCardClick: (measurementType: KClass<out Measurement>) -> Unit
) {
    val vitalsViewModel: HomeViewModel = hiltViewModel()

    val weekCards by vitalsViewModel.weekCardStates.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            text = "Измерения",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
        )

        if (weekCards.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items = weekCards, key = WeekCardState::key) { weekCard ->
                    with(weekCard) {
                        MeasurementCard(
                            modifier = Modifier.fillMaxWidth(),
                            measurementIcon = measurementIcon,
                            measurementUnit = measurementUnit.asText(),
                            measurementTitle = measurementTitle.asText(),
                            measurementValue = measurementValue?.asText(),
                            onClick = { onMeasurementCardClick(measurementType) },
                            yRange = if (points.isNotEmpty()) {
                                points.minOf { point -> point.y }..points.maxOf { point -> point.y }
                            } else {
                                0f..1f
                            },
                            chartDrawables = if (points.isNotEmpty()) {
                                listOf(
                                    CubicArea(
                                        points = points,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.0f),
                                            )
                                        )
                                    ),
                                    CubicLine(
                                        points = points,
                                        style = Stroke(width = 6.dp.value),
                                        color = MaterialTheme.colorScheme.primary,
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
                Text(
                    modifier = Modifier.padding(all = 24.dp),
                    text = "Пока нет ни одного измерения. Добавьте первое!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        }
    }
}
