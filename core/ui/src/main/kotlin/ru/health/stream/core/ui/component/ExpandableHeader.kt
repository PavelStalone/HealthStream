package ru.health.stream.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.KeyboardArrowDown
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.core.ui.theme.DeviceThemePreviews
import ru.health.stream.core.ui.theme.HealthStreamTheme

@Composable
fun ExpandableHeader(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    val expandIcon = @Composable {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .rotate(degrees = rotation),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            imageVector = Icons.Default.KeyboardArrowDown,
        )
    }

    Row(
        modifier = modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = null,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.Start,
            )
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            actions?.let { expandIcon() }
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.End,
            ),
        ) {
            if (actions != null) {
                actions()
            } else {
                expandIcon()
            }
        }
    }
}

@Composable
@DeviceThemePreviews
private fun ExpandableHeaderPreview() {
    val formatter = DateTimeComponents.Format {
        monthName(names = MonthNames.RUSSIAN_FULL)
        char(value = ' ')
        dayOfMonth(Padding.NONE)
        chars(value = ", ")
        year()
    }
    val now = Clock.System.now().format(formatter)

    var isExpand by remember { mutableStateOf(false) }

    HealthStreamTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExpandableHeader(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                title = now,
                isExpanded = isExpand,
                onClick = { isExpand = !isExpand },
            )
            ExpandableHeader(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                title = now,
                isExpanded = isExpand,
                onClick = { isExpand = !isExpand },
                actions = {
                    Text(
                        text = "Выбрать".uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            )
        }
    }
}
