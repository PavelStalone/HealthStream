package ru.health.stream.feature.onboarding.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import ru.health.stream.core.ui.composition.LocalLocale
import ru.health.stream.core.ui.composition.LocalTimeZone
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.content
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.core.chart.api.LineChart
import ru.health.stream.core.chart.core.ChartScope
import ru.health.stream.core.chart.core.Drawable
import ru.health.stream.feature.onboarding.impl.presentation.composition.LocalOnboardingScope

@Composable
internal fun MeasurementsCard(
    onClick: () -> Unit,
    measurementIcon: UiIcon,
    measurementUnit: String,
    measurementTitle: String,
    measurementValue: String?,
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    animation: Boolean = true,
    estimationLevel: UiLevel? = null,
    chartDrawables: List<Drawable> = emptyList(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    period: Period = Period.Week(firstDayOfWeek = firstDayOfWeek),
    chartContent: @Composable ChartScope.() -> Unit = {},
) {
    val locale = LocalLocale.current
    val timeZone = LocalTimeZone.current
    val onboardingScope = LocalOnboardingScope.current

    val display = remember(locale, period, timeZone, firstDayOfWeek) {
        when (period) {
            Period.Day -> Period.Day.getDisplay()
            Period.Month -> Period.Month.getDisplay(
                locale = locale,
                timeZone = timeZone,
                date = Clock.System.now(),
                firstDayOfWeek = firstDayOfWeek,
            )

            is Period.Week -> period.getDisplay(locale = locale)
            Period.Year -> Period.Year.getDisplay(locale = locale)
            else -> emptyMap()
        }
    }

    Card(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    measurementIcon.drawIcon(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = measurementTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
                estimationLevel?.content(
                    modifier = Modifier.onboardingTarget(
                        key = "estimation",
                        scope = onboardingScope
                    )
                )
            }

            if (measurementValue != null) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = measurementValue,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Text(
                        modifier = Modifier
                            .alignByBaseline()
                            .padding(start = 4.dp),
                        text = measurementUnit,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }

                if (chartDrawables.isNotEmpty()) {
                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        animation = animation,
                        xRange = 0f..1f,
                        yRange = yRange,
                        chartDrawables = chartDrawables,
                    ) {
                        chartContent()
                        display.forEach { (x, text) ->
                            Text(
                                modifier = Modifier.bindXAxis(
                                    x = x,
                                    alignment = when (x) {
                                        0f -> Alignment.End
                                        1f -> Alignment.Start
                                        else -> Alignment.CenterHorizontally
                                    }
                                ),
                                text = text,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                ),
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных за этот период",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }
        }
    }
}
