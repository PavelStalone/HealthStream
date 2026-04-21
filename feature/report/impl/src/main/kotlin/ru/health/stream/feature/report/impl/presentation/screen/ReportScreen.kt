package ru.health.stream.feature.report.impl.presentation.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
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
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.icon.default.Calendar
import ru.health.stream.core.ui.icon.default.KeyboardArrowDown
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.feature.report.impl.presentation.viewmodel.ReportUiEvent
import ru.health.stream.feature.report.impl.presentation.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val timeZone = LocalTimeZone.current

    val reportFormat by viewModel.reportFormat.collectAsStateWithLifecycle()
    val dateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val dataTypes by viewModel.selectedDataTypes.collectAsStateWithLifecycle()
    val groupMeasurements by viewModel.measurementsGroup.collectAsStateWithLifecycle()
    val bannedMeasurements by viewModel.bannedMeasurements.collectAsStateWithLifecycle()
    val expandedMeasurementGroup by viewModel.expandedMeasurementGroup.collectAsStateWithLifecycle()

    val dateState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = dateRange.start.toEpochMilliseconds(),
        initialSelectedEndDateMillis = dateRange.endInclusive.toEpochMilliseconds(),
    )

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReportUiEvent.ShareFile -> {
                    shareFile(context, event.uri, event.format)
                }
            }
        }
    }

    Column(modifier = modifier) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            title = UiText.NonTranslatable("Отчет"),
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.ArrowBack,
                    )
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Конфигурация отчета",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = "Период", style = MaterialTheme.typography.bodyMedium)
                    DateRange(
                        onClick = { showDatePicker = true },
                        prefixIcon = UiIcon.Vector(Icons.Default.Calendar),
                        actionIcon = UiIcon.Vector(Icons.Default.KeyboardArrowDown),
                        startDate = dateRange.start
                            .toLocalDateTime(timeZone).date
                            .format(dateFormatter),
                        endDate = dateRange.endInclusive
                            .toLocalDateTime(timeZone).date
                            .format(dateFormatter),
                    )

                    Text(
                        text = "Формат",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ReportFormat.entries.forEach { format ->
                            FilterChip(
                                selected = reportFormat == format,
                                onClick = { viewModel.onFormatChange(format) },
                                label = { Text(format.name) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Типы данных",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UiMeasurement.Type.entries.forEach { type ->
                            FilterChip(
                                selected = dataTypes.contains(type),
                                onClick = { viewModel.onDataTypeToggle(type) },
                                label = { Text(text = type.text.asText()) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.generateReport() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                        enabled = true
                    ) {
                        Text("Сгенерировать отчет", color = Color.White, fontSize = 18.sp)
                    }

                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Данные",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            groupMeasurements.forEach { measurementGroup ->
                with(measurementGroup) {
                    val isExpand = expandedMeasurementGroup.contains(id)
                    val measurementsId = measurements.map(UiMeasurement::id)

                    item(key = id) {
                        val isSectorBanned by remember {
                            derivedStateOf { bannedMeasurements.containsAll(measurementsId) }
                        }

                        ExpandableHeader(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .animateItem(),
                            isExpanded = isExpand,
                            title = date.format(dateFormatter),
                            onClick = { viewModel.expandMeasurementGroup(id) },
                            actions = {
                                TextButton(
                                    onClick = {
                                        if (isSectorBanned) {
                                            viewModel.unbanMeasurements(measurementsId)
                                        } else {
                                            viewModel.banMeasurements(measurementsId)
                                        }
                                    }
                                ) {
                                    AnimatedContent(
                                        targetState = isSectorBanned,
                                        transitionSpec = {
                                            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                                        }
                                    ) { isBanned ->
                                        if (isBanned) {
                                            Text(
                                                fontWeight = FontWeight.Bold,
                                                text = "Выбрать все".uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        } else {
                                            Text(
                                                fontWeight = FontWeight.Bold,
                                                text = "Исключить все".uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    if (isExpand) {
                        measurements.forEach { measurement ->
                            with(measurement) {
                                item(key = id) {
                                    val isMeasurementBanned = bannedMeasurements.contains(id)
                                    MeasurementCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem(),
                                        enabled = !isMeasurementBanned,
                                        type = type.text.asText(),
                                        unit = unit.asText(),
                                        time = time.toLocalDateTime(timeZone).format(timeFormatter),
                                        value = value,
                                        sourceIcon = resource.icon,
                                        sourceName = resource.text.asText(),
                                        measurementIcon = type.icon,
                                        note = note?.asText(),
                                        estimation = estimation,
                                        onEditClick = {},
                                        onDeleteClick = {},
                                        onCardClick = {
                                            if (isMeasurementBanned) {
                                                viewModel.unbanMeasurement(id)
                                            } else {
                                                viewModel.banMeasurement(id)
                                            }
                                        },
                                        actionIcon = {
                                            Checkbox(
                                                modifier = Modifier.fillMaxHeight(),
                                                checked = !isMeasurementBanned,
                                                onCheckedChange = null
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
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateState.selectedStartDateMillis?.let {
                            Instant.fromEpochMilliseconds(it)
                        }
                        val end =
                            dateState.selectedEndDateMillis?.let { Instant.fromEpochMilliseconds(it) }
                        if (start != null && end != null) {
                            viewModel.onDateRangeChange(start, end)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DateRangePicker(
                modifier = Modifier.weight(1f),
                state = dateState,
            )
        }
    }
}

private fun shareFile(context: Context, uri: Uri, format: ReportFormat) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (format == ReportFormat.PDF) "application/pdf" else "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Отправить отчет"))
}

@Composable
fun DateRange(
    startDate: String,
    endDate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prefixIcon: UiIcon? = null,
    actionIcon: UiIcon? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodyMedium
    val dateText = buildString {
        append(startDate)
        append(" - ")
        append(endDate)
    }
    val textSize = textMeasurer.measure(
        text = dateText,
        style = textStyle,
    ).size
    val textHeight = with(LocalDensity.current) { textSize.height.toDp() }

    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            prefixIcon?.drawIcon(
                modifier = Modifier
                    .sizeIn(maxHeight = textHeight)
                    .padding(end = 4.dp),
                tint = LocalContentColor.current.copy(alpha = 0.6f),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = dateText,
                style = textStyle,
                textAlign = TextAlign.Start,
            )
            actionIcon?.drawIcon(
                modifier = Modifier
                    .sizeIn(maxHeight = textHeight)
                    .padding(start = 4.dp),
                tint = LocalContentColor.current.copy(alpha = 0.6f),
            )
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

@Preview
@Composable
fun DateRangePreview() {
    HealthStreamTheme(
        dynamicColor = false
    ) {
        DateRange(
            modifier = Modifier.fillMaxWidth(),
            onClick = { },
            endDate = "End date",
            startDate = "Start date",
            prefixIcon = UiIcon.Vector(Icons.Default.Calendar),
            actionIcon = UiIcon.Vector(Icons.Default.KeyboardArrowDown),
        )
    }
}
