package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.collection.FloatFloatPair
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.component.ExpandableHeader
import ru.health.stream.core.ui.component.SectionHeader
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.onboarding.impl.presentation.component.MeasurementSwipeableCard
import ru.health.stream.feature.onboarding.impl.presentation.component.MeasurementTrendCard
import ru.health.stream.feature.onboarding.impl.presentation.component.onboardingTarget
import ru.health.stream.feature.onboarding.impl.presentation.composition.LocalOnboardingScope
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
internal fun OnboardingMeasurementScreen(
    viewModel: OnboardingViewModel
) {
    val onboardingScope = LocalOnboardingScope.current

    val currentStep by viewModel.currentStepFlow.collectAsState()

    val isExpanded by remember {
        derivedStateOf {
            listOf("measurement_expand", "measurement_expand_edit").contains(currentStep.id)
        }
    }
    val isEdit by remember { derivedStateOf { currentStep.id == "measurement_expand_edit" } }

    val options = listOf(
        UiText.NonTranslatable(value = "Сегодня"),
        UiText.NonTranslatable(value = "Неделя"),
        UiText.NonTranslatable(value = "Месяц"),
        UiText.NonTranslatable(value = "Год"),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            title = UiText.NonTranslatable("Пульс"),
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.ArrowBack,
                    )
                }
            },
            actions = {
                IconButton(
                    modifier = Modifier.onboardingTarget(
                        key = "measurement_add_button",
                        scope = onboardingScope,
                    ),
                    onClick = {}
                ) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.Add,
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size,
                            ),
                            onClick = {},
                            selected = index == 1,
                            icon = {},
                            colors = SegmentedButtonDefaults.colors(
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                )
                            )
                        ) {
                            Text(
                                text = option.asText(),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionHeader(text = "Динамика показателей")
                    Card(
                        modifier = Modifier.onboardingTarget(
                            key = "measurement_chart",
                            scope = onboardingScope,
                        ),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(all = 16.dp)) {
                            MeasurementTrendCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                yRange = 50f..120f,
                                chartDrawables = listOf(
                                    Scatter(
                                        positions = ranges,
                                        radiusPoint = 4.dp,
                                        pointColor = MaterialTheme.colorScheme.primary,
                                        rangeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    ),
                                    CubicLine(
                                        points = points,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        style = Stroke(width = 6f, cap = StrokeCap.Round),
                                    )
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SectionHeader(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .onboardingTarget(
                            key = "measurement_data",
                            scope = onboardingScope,
                        )
                        .padding(all = 8.dp),
                    text = "История измерений",
                )
            }

            item {
                ExpandableHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .padding(horizontal = 8.dp)
                        .onboardingTarget(key = "measurement_data_title", scope = onboardingScope)
                        .padding(all = 8.dp),
                    isExpanded = isExpanded,
                    title = "Сегодня",
                    onClick = {},
                )
            }

            if (isExpanded) {
                item(key = "Measurement") {
                    MeasurementSwipeableCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp)
                            .onboardingTarget(
                                key = "measurement_card",
                                scope = onboardingScope,
                            ),
                        isEdit = isEdit,
                        value = "82",
                        unit = "уд/мин",
                        note = "После пробежки",
                        estimation = UiLevel.NORMAL,
                        type = "Пульс",
                        sourceIcon = UiMeasurement.Resource.Manual.icon,
                        measurementIcon = UiIcon.Vector(Icons.Default.Favorite),
                        sourceName = "Ручная запись",
                        time = "10:30",
                    )
                }
            }

            item(key = "Yesterday") {
                ExpandableHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .padding(horizontal = 16.dp),
                    isExpanded = false,
                    title = "Вчера",
                    onClick = {},
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
