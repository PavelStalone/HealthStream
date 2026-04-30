package ru.health.stream.feature.measurement.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.ui.component.ExpandableHeader
import ru.health.stream.core.ui.component.MeasurementCard
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.measurement.impl.presentation.component.MeasurementTrendCard
import ru.health.stream.feature.measurement.impl.presentation.model.UiPeriod
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementViewModel
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementsChartState
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementsState
import kotlin.reflect.KClass

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MeasurementScreen(
    onBackClick: () -> Unit,
    measurementType: KClass<out Measurement>,
    addMeasurementClick: (KClass<out Measurement>) -> Unit,
    modifier: Modifier = Modifier,
    startPeriod: UiPeriod = UiPeriod.Week,
) {
    val timeZone = LocalTimeZone.current
    val viewModel: MeasurementViewModel = hiltViewModel()

    val chartState by viewModel.measurementChartStates.collectAsStateWithLifecycle()
    val measurementsState by viewModel.measurementsState.collectAsStateWithLifecycle()
    val expandedMeasurements by viewModel.expandedMeasurementsFlow.collectAsStateWithLifecycle()
    val periodState by viewModel.convertedPeriodFlow.collectAsStateWithLifecycle()

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
                .padding(all = 8.dp),
            title = UiText.NonTranslatable(
                value = measurementType.simpleName
                    ?: "Детали измерения" // TODO: Change title name - shoplikpavel 2026-03-31
            ),
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
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    onClick = { addMeasurementClick(measurementType) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
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
                Column {
                    Text(
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                        text = "Динамика показателей",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.padding(all = 20.dp)) {
                            when (val state = chartState) {
                                MeasurementsChartState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 3.dp)
                                    }
                                }

                                is MeasurementsChartState.Main -> {
                                    with(state) {
                                        MeasurementTrendCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp),
                                            period = periodState,
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

            item {
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = "История измерений",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }

            if (measurementsState is MeasurementsState.Main) {
                val state = measurementsState as MeasurementsState.Main

                state.measurements.forEach { group ->
                    val isExpanded = expandedMeasurements.contains(group.id)

                    item(key = group.id) {
                        ExpandableHeader(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .animateItem()
                                .padding(top = 16.dp),
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
                                        time = time.toLocalDateTime(timeZone).format(timeFormatter),
                                        onEditClick = {}, // TODO: Add on click method in edit callback - pavelshoplik 21-04-2026
                                        onDeleteClick = {}, // TODO: Add on click method in delete callback - pavelshoplik 21-04-2026
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
