package ru.health.stream.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Delete
import ru.health.stream.core.ui.icon.default.Edit
import ru.health.stream.core.ui.icon.device.PulseOximeter
import ru.health.stream.core.ui.icon.fill.Favorite
import ru.health.stream.core.ui.layout.SwipeableRow
import ru.health.stream.core.ui.model.UiIcon
import ru.health.stream.core.ui.model.UiLevel
import ru.health.stream.core.ui.model.content
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.core.ui.theme.DeviceThemePreviews
import ru.health.stream.core.ui.theme.HealthStreamTheme

@Composable
fun MeasurementCard(
    type: String,
    unit: String,
    time: String,
    value: String,
    sourceIcon: UiIcon,
    sourceName: String,
    measurementIcon: UiIcon,
    modifier: Modifier = Modifier,
    note: String? = null,
    swipeWidth: Dp = 168.dp,
    enabled: Boolean = true,
    estimation: UiLevel? = null,
    border: BorderStroke? = null,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCardClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    deleteColor: Color = MaterialTheme.colorScheme.error,
    editColor: Color = MaterialTheme.colorScheme.primary,
    deleteBackgroundColor: Color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
    editBackgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    actionIcon: @Composable () -> Unit = {},
) {
    val secondaryContentColor = colors.contentColor.copy(alpha = 0.7f)

    val clickableModifier = onCardClick?.let {
        Modifier
            .clip(shape)
            .clickable(onClick = onCardClick)
    } ?: Modifier

    Card(
        modifier = modifier.then(clickableModifier),
        shape = shape,
        border = border,
        elevation = elevation,
        colors = colors.copy(
            containerColor = if (enabled) {
                colors.containerColor
            } else {
                colors.disabledContentColor
            }
        ),
    ) {
        SwipeableRow(
            swipeWidth = swipeWidth,
            mainContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            measurementIcon.drawIcon(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxHeight()
                                    .background(
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    )
                                    .padding(12.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = type.uppercase(),
                                        color = colors.contentColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                    estimation?.content()
                                }
                                Row(verticalAlignment = Alignment.Bottom) {
                                    val textMeasurer = rememberTextMeasurer()
                                    val textStyle = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                    val textLayout = textMeasurer.measure(
                                        text = value,
                                        style = textStyle,
                                    )
                                    val yOffset = with(LocalDensity.current) {
                                        (textLayout.size.height / 6).toDp()
                                    }

                                    Text(
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .offset(y = yOffset),
                                        text = value,
                                        style = textStyle,
                                        color = colors.contentColor,
                                    )
                                    Text(
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .padding(start = 4.dp),
                                        text = unit,
                                        color = secondaryContentColor,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            Text(
                                text = time,
                                color = colors.contentColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Layout(
                                content = {
                                    sourceIcon.drawIcon(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillMaxHeight(),
                                        tint = secondaryContentColor,
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = sourceName,
                                        color = secondaryContentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            ) { measurables, constraints ->
                                val icon = measurables.first()
                                val text = measurables.last()

                                val textPlaceable = text.measure(constraints)
                                val iconPlaceable =
                                    icon.measure(constraints.copy(maxHeight = textPlaceable.height))

                                layout(
                                    width = textPlaceable.width + iconPlaceable.width,
                                    height = textPlaceable.height
                                ) {
                                    iconPlaceable.placeRelative(x = 0, y = 0)
                                    textPlaceable.placeRelative(x = iconPlaceable.width, y = 0)
                                }
                            }
                            actionIcon()
                        }
                    }
                    note?.let { note ->
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = note,
                            color = secondaryContentColor,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        ) { progress ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = editBackgroundColor)
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        modifier = Modifier.graphicsLayer {
                            alpha = progress
                            scaleX = 0.8f + 0.2f * progress
                            scaleY = 0.8f + 0.2f * progress
                        },
                        tint = editColor,
                        contentDescription = null,
                        imageVector = Icons.Default.Edit,
                    )
                    Text(
                        modifier = Modifier.graphicsLayer {
                            alpha = progress
                            scaleX = 0.2f + 0.8f * progress
                            scaleY = 0.8f + 0.2f * progress
                        },
                        softWrap = false,
                        text = "Изменить".uppercase(),
                        color = editColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = deleteBackgroundColor)
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        modifier = Modifier.graphicsLayer {
                            alpha = progress
                            scaleX = 0.8f + 0.2f * progress
                            scaleY = 0.8f + 0.2f * progress
                        },
                        tint = deleteColor,
                        contentDescription = null,
                        imageVector = Icons.Default.Delete,
                    )
                    Text(
                        modifier = Modifier.graphicsLayer {
                            alpha = progress
                            scaleX = 1f * progress
                            scaleY = 0.8f + 0.2f * progress
                        },
                        softWrap = false,
                        text = "Удалить".uppercase(),
                        color = deleteColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
@DeviceThemePreviews
private fun PreviewMeasurementCard() {
    HealthStreamTheme(
        dynamicColor = false,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MeasurementCard(
                modifier = Modifier.fillMaxWidth(),
                sourceIcon = UiIcon.Vector(Icons.Device.PulseOximeter),
                measurementIcon = UiIcon.Vector(Icons.Fill.Favorite),
                unit = "уд/мин",
                time = "12:30",
                value = "72",
                sourceName = "Пульсоксиметр",
                type = "Пульс",
                note = "Текст заметки",
                estimation = UiLevel.NORMAL,
            )

            MeasurementCard(
                modifier = Modifier.fillMaxWidth(),
                sourceIcon = UiIcon.Vector(Icons.Device.PulseOximeter),
                measurementIcon = UiIcon.Vector(Icons.Fill.Favorite),
                unit = "уд/мин",
                time = "12:30",
                value = "72",
                sourceName = "Пульсоксиметр",
                type = "Пульс",
                estimation = UiLevel.NORMAL,
            )

            MeasurementCard(
                modifier = Modifier.fillMaxWidth(),
                sourceIcon = UiIcon.Vector(Icons.Device.PulseOximeter),
                measurementIcon = UiIcon.Vector(Icons.Fill.Favorite),
                unit = "уд/мин",
                time = "12:30",
                value = "72",
                sourceName = "Пульсоксиметр",
                type = "Пульс",
            )

            MeasurementCard(
                modifier = Modifier.fillMaxWidth(),
                sourceIcon = UiIcon.Vector(Icons.Device.PulseOximeter),
                measurementIcon = UiIcon.Vector(Icons.Fill.Favorite),
                unit = "уд/мин",
                time = "12:30",
                value = "72",
                sourceName = "Пульсоксиметр",
                type = "Пульс",
                note = "Текст заметки",
                estimation = UiLevel.NORMAL,
                actionIcon = {
                    UiIcon.Vector(Icons.Fill.Favorite).drawIcon(
                        modifier = Modifier.fillMaxHeight(),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            )
        }
    }
}
