package ru.health.stream.feature.vitals.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.asText
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.vitals.data.model.Period
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement
import ru.health.stream.feature.vitals.ui.component.MeasurementTrendCard
import ru.health.stream.feature.vitals.ui.model.UiPeriod
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeasurementScreen(
    measurementType: KClass<out HealthMeasurement>,
    modifier: Modifier = Modifier,
    startPeriod: UiPeriod = UiPeriod.Week,
) {
    val options = remember { listOf(UiPeriod.Today, UiPeriod.Week, UiPeriod.Month, UiPeriod.Year) }
    var selectedPeriod by remember { mutableStateOf(startPeriod) }

    val viewModel: MeasurementViewModel = hiltViewModel(
        creationCallback = { factory: MeasurementViewModel.Factory ->
            factory.create(period = startPeriod, measurementType = measurementType)
        }
    )

    val chartState by viewModel.measurementChartStates.collectAsStateWithLifecycle()
    val measurementsState by viewModel.measurementsState.collectAsStateWithLifecycle()
    val expandedMeasurements by viewModel.expandedMeasurementsFlow.collectAsStateWithLifecycle()
    val periodState by viewModel.convertedPeriodFlow.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
                space = 8.dp
            ) {
                options.forEach { option ->
                    SegmentedButton(
                        shape = RoundedCornerShape(4.dp),
                        onClick = {
                            selectedPeriod = option
                            viewModel.changePeriod(option)
                        },
                        selected = option == selectedPeriod,
                        icon = {}
                    ) {
                        Text(text = option.label.asText())
                    }
                }
            }
        }

        item {
            Text(
                text = "Graph",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val state = chartState) {
                MeasurementsChartState.Loading -> {
                    MeasurementTrendCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        yRange = 0f..1f,
                        animation = false,
                        period = Period.Day
                    )
                }

                is MeasurementsChartState.Main -> {
                    with(state) {
                        MeasurementTrendCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            animation = true,
                            period = periodState,
                            yRange = drawableData.yRange,
                            chartDrawables = drawableData.positions.flatMap { positions ->
                                listOf(
                                    Scatter(
                                        positions = positions,
                                        pointColor = Color.Red,
                                        rangeColor = Color.Red.copy(alpha = 0.3f),
                                        radiusPoint = 6.dp
                                    ),
                                    CubicLine(
                                        points = positions.map { position ->
                                            when (position) {
                                                is ChartPosition.Point -> position
                                                is ChartPosition.Range.Horizontal -> ChartPosition.Point(
                                                    position.averageX,
                                                    position.y
                                                )

                                                is ChartPosition.Range.Vertical -> ChartPosition.Point(
                                                    position.x,
                                                    position.averageY
                                                )
                                            }
                                        },
                                        color = Color.White,
                                        style = Stroke(width = 3f, cap = StrokeCap.Round),
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Values",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        when (val state = measurementsState) {
            MeasurementsState.Loading -> {}
            is MeasurementsState.Main -> {
                val formatter = DateTimeComponents.Format {
                    monthName(MonthNames.ENGLISH_FULL)
                    char(' ')
                    dayOfMonth(padding = Padding.NONE)
                    chars(", ")
                    year()
                }

                state.measurements.forEach { group ->
                    item(key = group.id) {
                        MeasurementDateGroup(
                            modifier = Modifier.animateItem(),
                            date = group.date.format(formatter),
                            isExpanded = expandedMeasurements.contains(group.id),
                            onClick = { viewModel.expandMeasurement(group.id) }
                        )
                    }

                    if (expandedMeasurements.contains(group.id)) {
                        group.measurements.forEach { measurement ->
                            item(key = measurement.id) {
                                with(measurement) {
                                    MeasurementItem(
                                        modifier = Modifier.animateItem(),
                                        icon = resourceIcon,
                                        sourceName = resourceTitle.asText(),
                                        time = createdAt.format(formatter),
                                        type = title.asText(),
                                        value = value.asText(),
                                        unit = unit.asText(),
                                        note = note?.asText(),
                                        showActions = false
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementDateGroup(
    date: String,
    onClick: () -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }
}

@Composable
fun MeasurementItem(
    icon: UiIcon,
    sourceName: String,
    time: String,
    type: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    status: String? = null,
    showActions: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        icon.drawIcon(
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = sourceName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    status?.let {
                        Surface(
                            color = Color(0xFFFFE0B2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showActions) {
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFFFECB3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = Color.DarkGray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFFFCDD2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}
