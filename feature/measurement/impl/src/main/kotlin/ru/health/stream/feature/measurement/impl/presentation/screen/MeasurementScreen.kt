package ru.health.stream.feature.measurement.impl.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.icon.default.Delete
import ru.health.stream.core.ui.icon.default.Edit
import ru.health.stream.core.ui.icon.default.KeyboardArrowDown
import ru.health.stream.core.ui.layout.RowByFirstBaseLine
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText
import ru.health.stream.core.ui.model.content
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.measurement.impl.presentation.component.MeasurementTrendCard
import ru.health.stream.feature.measurement.impl.presentation.model.UiPeriod
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementViewModel
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementsChartState
import ru.health.stream.feature.measurement.impl.presentation.viewmodel.MeasurementsState
import kotlin.math.roundToInt
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
    val viewModel: MeasurementViewModel = hiltViewModel(
        creationCallback = { factory: MeasurementViewModel.Factory ->
            factory.create(period = startPeriod, measurementType = measurementType)
        }
    )

    val chartState by viewModel.measurementChartStates.collectAsStateWithLifecycle()
    val measurementsState by viewModel.measurementsState.collectAsStateWithLifecycle()
    val expandedMeasurements by viewModel.expandedMeasurementsFlow.collectAsStateWithLifecycle()
    val periodState by viewModel.convertedPeriodFlow.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf(value = startPeriod) }
    val options = listOf(UiPeriod.Today, UiPeriod.Week, UiPeriod.Month, UiPeriod.Year)

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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
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
                                            chartDrawables = drawableData.positions.flatMap { positions ->
                                                listOf(
                                                    Scatter(
                                                        positions = positions,
                                                        pointColor = MaterialTheme.colorScheme.primary,
                                                        rangeColor = MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.3f
                                                        ),
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
                                                        color = MaterialTheme.colorScheme.tertiary,
                                                        style = Stroke(
                                                            width = 6f,
                                                            cap = StrokeCap.Round
                                                        ),
                                                    )
                                                )
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
                val formatter = DateTimeComponents.Format {
                    monthName(names = MonthNames.RUSSIAN_FULL)
                    char(value = ' ')
                    dayOfMonth(Padding.NONE)
                    chars(value = ", ")
                    year()
                }
                val timeFormatter = DateTimeComponents.Format {
                    hour()
                    char(value = ':')
                    minute()
                }

                state.measurements.forEach { group ->
                    val isExpanded = expandedMeasurements.contains(group.id)

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    item(key = group.id) {
                        val cardShape = if (isExpanded) {
                            MaterialTheme.shapes.large.copy(
                                bottomEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(0.dp)
                            )
                        } else {
                            MaterialTheme.shapes.large
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = cardShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                        ) {
                            MeasurementDateHeader(
                                date = group.date.format(formatter),
                                isExpanded = isExpanded,
                                onClick = { viewModel.expandMeasurement(group.id) }
                            )
                        }
                    }

                    if (isExpanded) {
                        group.measurements.forEachIndexed { index, measurement ->
                            val isLast = index == group.measurements.lastIndex

                            with(measurement) {
                                item(key = id) {
                                    val cardShape = if (isLast) {
                                        MaterialTheme.shapes.large.copy(
                                            topEnd = CornerSize(0.dp),
                                            topStart = CornerSize(0.dp),
                                        )
                                    } else {
                                        RectangleShape
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem(),
                                        shape = cardShape,
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.2f
                                            )
                                        ),
                                    ) {
                                        SwipeableMeasurementRow(
                                            onEditClick = {
                                                // TODO: Handle edit click
                                            },
                                            onDeleteClick = {
                                                // TODO: Handle delete click
                                            }
                                        ) {
                                            MeasurementRow(
                                                modifier = Modifier,
                                                type = type,
                                                isLast = isLast,
                                                icon = resourceIcon,
                                                unit = unit.asText(),
                                                note = note?.asText(),
                                                value = value.asText(),
                                                estimation = estimation,
                                                sourceName = resourceTitle.asText(),
                                                time = createdAt.format(timeFormatter),
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
}

@Composable
private fun MeasurementDateHeader(
    date: String,
    onClick: () -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Icon(
            modifier = Modifier
                .size(24.dp)
                .rotate(degrees = rotation),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun MeasurementRow(
    icon: UiIcon,
    unit: String,
    time: String,
    value: String,
    isLast: Boolean,
    sourceName: String,
    type: KClass<out Measurement>,
    modifier: Modifier = Modifier,
    note: String? = null,
    estimation: UiLevel? = null,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                icon.drawIcon(
                    modifier = Modifier.size(size = 24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = time,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = type.simpleName
                        ?: "Неизестно", // TODO: Change text - shoplikpavel 2026-03-17
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                )
                estimation?.content()
            }
            RowByFirstBaseLine {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
            note?.let { note ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = note,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                )
            }
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun SwipeableMeasurementRow(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val actionWidth = 40.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }

    val anchors = remember(actionWidthPx) {
        DraggableAnchors {
            DragState.CLOSED at 0f
            DragState.OPENED at -actionWidthPx
        }
    }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragState.CLOSED,
            anchors = anchors
        )
    }

    val offset = state.offset
    val progress = (-offset / actionWidthPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionWidth * progress)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color = Color(color = 0xFFFFECB3))
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.graphicsLayer {
                        alpha = progress
                        scaleX = 0.8f + 0.2f * progress
                        scaleY = 0.8f + 0.2f * progress
                    },
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color = Color(color = 0xFFFFCDD2))
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.graphicsLayer {
                        alpha = progress
                        scaleX = 0.8f + 0.2f * progress
                        scaleY = 0.8f + 0.2f * progress
                    },
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(x = offset.roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                )
        ) {
            content()
        }
    }
}

private enum class DragState {
    CLOSED,
    OPENED,
    ;
}
