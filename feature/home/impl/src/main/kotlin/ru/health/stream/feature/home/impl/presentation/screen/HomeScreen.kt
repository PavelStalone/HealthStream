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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.icon.default.Report
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.core.chart.core.drawable.CubicLine
import ru.health.stream.core.chart.core.drawable.Scatter
import ru.health.stream.feature.home.impl.presentation.component.MeasurementCard
import ru.health.stream.feature.home.impl.presentation.viewmodel.HomeViewModel
import ru.health.stream.feature.home.impl.presentation.viewmodel.WeekCardState
import kotlin.reflect.KClass

@Composable
internal fun HomeScreen(
    onReportIconClick: () -> Unit,
    onAddMeasurementIconClick: () -> Unit,
    onMeasurementCardClick: (measurementType: KClass<out Measurement>) -> Unit,
) {
    val vitalsViewModel: HomeViewModel = hiltViewModel()

    val weekCards by vitalsViewModel.weekCardStates.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            title = UiText.NonTranslatable(value = "Измерения"),
            navigationIcon = {
                IconButton(
                    onClick = onReportIconClick
                ) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.Report,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onAddMeasurementIconClick
                ) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.Add,
                    )
                }
            }
        )

        if (weekCards.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = weekCards, key = WeekCardState::key) { weekCard ->
                    with(weekCard) {
                        MeasurementCard(
                            modifier = Modifier.fillMaxWidth(),
                            estimationLevel = estimationLevel,
                            measurementIcon = measurementIcon,
                            measurementUnit = measurementUnit.asText(),
                            measurementTitle = measurementTitle.asText(),
                            measurementValue = measurementValue?.asText(),
                            onClick = { onMeasurementCardClick(measurementType) },
                            yRange = drawableData.yRange,
                            chartDrawables = buildList {
                                drawableData.scatterPositions.forEach { positions ->
                                    add(
                                        Scatter(
                                            positions = positions,
                                            pointColor = MaterialTheme.colorScheme.primary,
                                            rangeColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.3f
                                            ),
                                            radiusPoint = 4.dp
                                        )
                                    )
                                }
                                drawableData.pointPositions.forEach { positions ->
                                    add(
                                        CubicLine(
                                            points = positions,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            style = Stroke(
                                                width = 6f,
                                                cap = StrokeCap.Round
                                            ),
                                        )
                                    )
                                }
                            },
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
