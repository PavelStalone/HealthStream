package ru.health.stream.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.Add
import ru.health.stream.core.ui.model.UiMeasurement
import ru.health.stream.core.ui.model.drawIcon
import ru.health.stream.core.ui.theme.DeviceThemePreviews
import ru.health.stream.core.ui.theme.HealthStreamTheme

private const val AnimateDuration = 200

/**
 * Reusable expandable FloatingActionButton component with mini action buttons
 */
@Composable
fun <T> ExpandableFloatingActionButton(
    items: List<T>,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    mainFabModifier: Modifier = Modifier,
    isExpanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    itemIcon: @Composable (T) -> Unit,
    mainFabIcon: @Composable (rotation: Float) -> Unit = { rotation ->
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Large floating action button",
            modifier = Modifier.rotate(rotation),
        )
    },
) {
    var internalExpanded by remember { mutableStateOf(false) }
    val expanded = isExpanded ?: internalExpanded

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation",
    )

    val toggleExpand: () -> Unit = {
        if (isExpanded == null) {
            internalExpanded = !internalExpanded
            onExpandedChange?.invoke(internalExpanded)
        } else {
            onExpandedChange?.invoke(!isExpanded)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEach { item ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(AnimateDuration)) +
                        slideInVertically(tween(AnimateDuration)) { it } +
                        scaleIn(
                            tween(AnimateDuration),
                            transformOrigin = TransformOrigin(0.5f, 0f),
                        ),
                exit = fadeOut(tween(AnimateDuration)) +
                        slideOutVertically(tween(AnimateDuration)) { it } +
                        scaleOut(
                            tween(AnimateDuration),
                            transformOrigin = TransformOrigin(0.5f, 0f),
                        ),
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        if (isExpanded == null) {
                            internalExpanded = false
                        }
                        onExpandedChange?.invoke(false)
                        onItemClick(item)
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    itemIcon(item)
                }
            }
        }

        FloatingActionButton(
            modifier = mainFabModifier,
            onClick = toggleExpand,
            shape = CircleShape,
        ) {
            mainFabIcon(rotation)
        }
    }
}

/**
 * Specialized FAB component for adding health measurements
 */
@Composable
fun AddMeasurementFab(
    onMeasurementTypeClick: (UiMeasurement.Type) -> Unit,
    modifier: Modifier = Modifier,
    mainFabModifier: Modifier = Modifier,
    isExpanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    measurementTypes: List<UiMeasurement.Type> = UiMeasurement.Type.entries,
) {
    ExpandableFloatingActionButton(
        modifier = modifier,
        mainFabModifier = mainFabModifier,
        items = measurementTypes,
        onItemClick = onMeasurementTypeClick,
        isExpanded = isExpanded,
        onExpandedChange = onExpandedChange,
        itemIcon = { type ->
            type.icon.drawIcon()
        },
    )
}

@Composable
@DeviceThemePreviews
private fun ExpandableFloatingActionButtonPreview() {
    HealthStreamTheme(dynamicColor = false) {
        AddMeasurementFab(
            isExpanded = true,
            onMeasurementTypeClick = {},
        )
    }
}
