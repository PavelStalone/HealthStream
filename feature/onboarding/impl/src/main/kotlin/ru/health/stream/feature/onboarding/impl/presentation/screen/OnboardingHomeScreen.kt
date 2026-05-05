package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.collection.FloatFloatPair
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.icon.default.Report
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.onboarding.impl.presentation.component.MeasurementsCard
import ru.health.stream.feature.onboarding.impl.presentation.component.onboardingTarget
import ru.health.stream.feature.onboarding.impl.presentation.composition.LocalOnboardingScope
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
internal fun OnboardingHomeScreen(
    viewModel: OnboardingViewModel
) {
    val onboardingScope = LocalOnboardingScope.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val currentStep by viewModel.currentStepFlow.collectAsState()

    var drawableData by remember { mutableStateOf(emptyList<Drawable>()) }

    LaunchedEffect(currentStep.id) {
        if (currentStep.id == "home_filled") {
            drawableData = listOf(
                Scatter(
                    positions = ranges,
                    pointColor = primaryColor,
                    rangeColor = primaryColor.copy(alpha = 0.3f),
                    radiusPoint = 4.dp
                ),
                CubicLine(
                    points = points,
                    color = tertiaryColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round),
                )
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            title = UiText.NonTranslatable(value = "Измерения"),
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.Report,
                    )
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.Add,
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MeasurementsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onboardingTarget("vitals_card", onboardingScope),
                    onClick = {},
                    measurementIcon = UiIcon.Vector(imageVector = Icons.Default.Favorite),
                    measurementUnit = "уд/мин",
                    measurementTitle = "Пульс",
                    measurementValue = if (drawableData.isNotEmpty()) "82" else null,
                    yRange = 50f..120f,
                    estimationLevel = UiLevel.NORMAL,
                    chartDrawables = drawableData,
                )
            }
        }
    }
}

private val points = listOf(
    ChartPosition.Point(x = 0.00f, y = 54f),
    ChartPosition.Point(x = 0.04f, y = 59f),
    ChartPosition.Point(x = 0.08f, y = 55f),
    ChartPosition.Point(x = 0.12f, y = 75f),
    ChartPosition.Point(x = 0.16f, y = 63f),
    ChartPosition.Point(x = 0.20f, y = 86f),
    ChartPosition.Point(x = 0.24f, y = 83f),
    ChartPosition.Point(x = 0.28f, y = 84f),
    ChartPosition.Point(x = 0.32f, y = 71f),
    ChartPosition.Point(x = 0.36f, y = 63f),
    ChartPosition.Point(x = 0.40f, y = 67f),
    ChartPosition.Point(x = 0.44f, y = 84f),
    ChartPosition.Point(x = 0.48f, y = 90f),
    ChartPosition.Point(x = 0.52f, y = 72f),
    ChartPosition.Point(x = 0.56f, y = 70f),
    ChartPosition.Point(x = 0.60f, y = 82f),
)

private val ranges = listOf(
    ChartPosition.Range.Vertical(x = 0.00f, y = FloatFloatPair(50f, 58f)),
    ChartPosition.Range.Vertical(x = 0.04f, y = FloatFloatPair(50f, 63f)),
    ChartPosition.Range.Vertical(x = 0.08f, y = FloatFloatPair(53f, 62f)),
    ChartPosition.Range.Vertical(x = 0.12f, y = FloatFloatPair(63f, 77f)),
    ChartPosition.Range.Vertical(x = 0.16f, y = FloatFloatPair(60f, 70f)),
    ChartPosition.Range.Vertical(x = 0.20f, y = FloatFloatPair(63f, 110f)),
    ChartPosition.Range.Vertical(x = 0.24f, y = FloatFloatPair(61f, 96f)),
    ChartPosition.Range.Vertical(x = 0.28f, y = FloatFloatPair(68f, 112f)),
    ChartPosition.Range.Vertical(x = 0.32f, y = FloatFloatPair(64f, 81f)),
    ChartPosition.Range.Vertical(x = 0.36f, y = FloatFloatPair(50f, 80f)),
    ChartPosition.Range.Vertical(x = 0.40f, y = FloatFloatPair(65f, 85f)),
    ChartPosition.Range.Vertical(x = 0.44f, y = FloatFloatPair(68f, 93f)),
    ChartPosition.Range.Vertical(x = 0.48f, y = FloatFloatPair(78f, 97f)),
    ChartPosition.Range.Vertical(x = 0.52f, y = FloatFloatPair(62f, 87f)),
    ChartPosition.Range.Vertical(x = 0.56f, y = FloatFloatPair(60f, 85f)),
    ChartPosition.Range.Vertical(x = 0.60f, y = FloatFloatPair(64f, 97f)),
)
