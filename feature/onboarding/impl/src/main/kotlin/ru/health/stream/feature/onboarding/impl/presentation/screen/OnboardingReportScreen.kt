package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.component.ExpandableHeader
import ru.health.stream.core.ui.component.MeasurementCard
import ru.health.stream.core.ui.component.SectionHeader
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.icon.default.Calendar
import ru.health.stream.core.ui.icon.default.Favorite
import ru.health.stream.core.ui.icon.default.KeyboardArrowDown
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText
import ru.health.stream.data.report.model.ReportFormat
import ru.health.stream.feature.onboarding.impl.presentation.component.onboardingTarget
import ru.health.stream.feature.onboarding.impl.presentation.composition.LocalOnboardingScope
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
internal fun OnboardingReportScreen(
    viewModel: OnboardingViewModel
) {
    val onboardingScope = LocalOnboardingScope.current

    val currentStep by viewModel.currentStepFlow.collectAsState()

    val isExpanded by remember { derivedStateOf { currentStep.id == "report_expand" } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            title = UiText.NonTranslatable("Отчет"),
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.ArrowBack,
                    )
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SectionHeader(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .padding(horizontal = 8.dp),
                        text = "Конфигурация",
                    )
                    Column {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Период",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        DateRangePlaceholder(
                            modifier = Modifier
                                .height(TextFieldDefaults.MinHeight)
                                .onboardingTarget(
                                    key = "report_date_range",
                                    scope = onboardingScope
                                )
                                .padding(all = 8.dp),
                            startDate = "1 мая 2026",
                            endDate = "7 мая 2026"
                        )
                    }

                    Text(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .padding(horizontal = 8.dp),
                        text = "Формат",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onboardingTarget(
                                key = "report_type_selection",
                                scope = onboardingScope,
                            )
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ReportFormat.entries.forEach { format ->
                            FilterChip(
                                selected = format == ReportFormat.PDF,
                                onClick = { },
                                label = { Text(text = format.name) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Text(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .padding(horizontal = 8.dp),
                        text = "Типы данных",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onboardingTarget(
                                key = "report_measurement_selection",
                                scope = onboardingScope,
                            )
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UiMeasurement.Type.entries.forEach { type ->
                            FilterChip(
                                selected = type == UiMeasurement.Type.HEART_RATE,
                                onClick = {},
                                label = { Text(text = type.text.asText()) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onboardingTarget(
                                key = "report_generate_button",
                                scope = onboardingScope,
                            )
                            .padding(all = 8.dp)
                            .height(TextFieldDefaults.MinHeight),
                        onClick = { },
                    ) {
                        Text(
                            text = "Сгенерировать отчет",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    SectionHeader(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .padding(horizontal = 8.dp),
                        text = "Данные",
                    )
                }
            }

            item {
                ExpandableHeader(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    isExpanded = isExpanded,
                    title = "Сегодня",
                    onClick = { },
                    actions = {
                        TextButton(
                            modifier = Modifier.onboardingTarget(
                                key = "report_title_exclude",
                                scope = onboardingScope,
                            ),
                            onClick = {}
                        ) {
                            Text(
                                text = "Исключить все".uppercase(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                )
            }

            if (isExpanded) {
                item {
                    MeasurementCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .onboardingTarget(key = "report_card", scope = onboardingScope),
                        enabled = true,
                        type = "Пульс",
                        unit = "уд/мин",
                        time = "12:00",
                        value = "82",
                        sourceIcon = UiMeasurement.Resource.Manual.icon,
                        sourceName = "Ручная запись",
                        measurementIcon = UiIcon.Vector(Icons.Default.Favorite),
                        estimation = UiLevel.NORMAL,
                        onEditClick = {},
                        onDeleteClick = {},
                        onCardClick = { },
                        actionIcon = {
                            Checkbox(
                                checked = true,
                                onCheckedChange = null
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun DateRangePlaceholder(
    startDate: String,
    endDate: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Calendar,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "$startDate - $endDate", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}
