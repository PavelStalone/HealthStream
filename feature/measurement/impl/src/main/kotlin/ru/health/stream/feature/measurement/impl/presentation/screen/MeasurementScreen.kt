package ru.health.stream.feature.measurement.impl.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.ui.component.ExpandableHeader
import ru.health.stream.core.ui.component.MeasurementCard
import ru.health.stream.core.ui.component.SectionHeader
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.core.ui.model.asText
import ru.health.stream.core.ui.model.asUi
import ru.health.stream.core.ui.modifier.shimmer
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.measurement.impl.R
import ru.health.stream.feature.measurement.impl.presentation.component.MeasurementTrendCard
import ru.health.stream.feature.measurement.impl.presentation.model.UiPeriod
import ru.health.stream.feature.measurement.impl.presentation.model.asPeriod
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementViewModel
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementsState
import kotlin.reflect.KClass

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MeasurementScreen(
    onBackClick: () -> Unit,
    onEditClick: (Measurement) -> Unit,
    measurementType: KClass<out Measurement>,
    addMeasurementClick: (KClass<out Measurement>) -> Unit,
    modifier: Modifier = Modifier,
    startPeriod: UiPeriod = UiPeriod.Week,
) {
    val timeZone = LocalTimeZone.current
    val viewModel: MeasurementViewModel = hiltViewModel()

    val measurementState by viewModel.measurementStateFlow.collectAsStateWithLifecycle()
    val expandedMeasurements by viewModel.expandedMeasurementsFlow.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf(value = startPeriod) }
    val options = listOf(UiPeriod.Today, UiPeriod.Week, UiPeriod.Month, UiPeriod.Year)

    LaunchedEffect(Unit) {
        viewModel.changeMeasurementType(measurementType = measurementType)
        viewModel.changePeriod(period = startPeriod)
    }

    Column(modifier = modifier) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            title = measurementType.asUi().text,
            navigationIcon = {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.ArrowBack,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { addMeasurementClick(measurementType) }
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
            contentPadding = PaddingValues(all = 16.dp),
        ) {
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size,
                            ),
                            onClick = {
                                selectedPeriod = option
                                viewModel.changePeriod(option)
                            },
                            selected = option == selectedPeriod,
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
                                text = option.label.asText(),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionHeader(text = "Динамика показателей")
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.padding(all = 16.dp)) {
                            AnimatedContent(targetState = measurementState) { state ->
                                when (state) {
                                    MeasurementsState.Empty -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "Нет данных за этот период",
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.outline,
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                        }
                                    }

                                    MeasurementsState.Loading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            val composition by rememberLottieComposition(
                                                LottieCompositionSpec.RawRes(R.raw.aggregate)
                                            )
                                            LottieAnimation(
                                                modifier = Modifier.fillMaxSize(),
                                                composition = composition,
                                                iterations = LottieConstants.IterateForever,
                                            )
                                        }
                                    }

                                    is MeasurementsState.Main -> with(state) {
                                        MeasurementTrendCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp),
                                            period = selectedPeriod.asPeriod(firstDayOfWeek = DayOfWeek.MONDAY),
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
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SectionHeader(text = "История измерений") }

            when (val state = measurementState) {
                MeasurementsState.Empty -> {
                    /* Do nothing */
                }

                MeasurementsState.Loading -> {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .shimmer()
                        )
                    }
                }

                is MeasurementsState.Main -> {
                    state.measurements.forEach { group ->
                        val isExpanded = expandedMeasurements.contains(group.id)

                        item(key = group.id) {
                            ExpandableHeader(
                                modifier = Modifier
                                    .height(32.dp)
                                    .fillMaxWidth()
                                    .animateItem()
                                    .padding(top = 8.dp),
                                isExpanded = isExpanded,
                                title = group.date.format(dateFormatter),
                                onClick = { viewModel.expandMeasurement(group.id) },
                            )
                        }

                        if (isExpanded) {
                            group.measurements.forEach { measurement ->
                                with(measurement) {
                                    item(key = id) {
                                        MeasurementCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem()
                                                .padding(top = 8.dp),
                                            value = value,
                                            unit = unit.asText(),
                                            note = note?.asText(),
                                            estimation = estimation,
                                            type = type.text.asText(),
                                            sourceIcon = resource.icon,
                                            measurementIcon = type.icon,
                                            sourceName = resource.text.asText(),
                                            time = time.toLocalDateTime(timeZone)
                                                .format(timeFormatter),
                                            onEditClick = {
                                                viewModel.editMeasurement(
                                                    uiMeasurement = measurement,
                                                    onEdit = onEditClick,
                                                )
                                            },
                                            onDeleteClick = {
                                                viewModel.deleteMeasurement(uiMeasurement = measurement)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val dateFormatter = LocalDate.Format {
    dayOfMonth(Padding.NONE)
    char(value = ' ')
    monthName(names = MonthNames.RUSSIAN_FULL)
    char(value = ' ')
    year()
}
private val timeFormatter = LocalDateTime.Format {
    hour()
    char(value = ':')
    minute()
}
