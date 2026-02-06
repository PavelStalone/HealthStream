package ru.health.stream.core.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout

@Composable
fun RowByFirstBaseLine(
    modifier: Modifier,
    content: @Composable () -> Unit,
) = Layout(
    modifier = modifier,
    content = content,
) { measurables, constraints ->

    val placeables = measurables.map { measurable ->
        val placeable = measurable.measure(constraints)
        val baseLine = placeable[FirstBaseline].let { line ->
            if (line == AlignmentLine.Unspecified) placeable.height else line
        }

        baseLine to placeable
    }

    val maxBaseLine = placeables.maxOf { (baseLine, _) -> baseLine }

    layout(
        width = placeables.fold(0) { acc, (_, placeable) -> acc + placeable.width },
        height = placeables.maxOf { (_, placeable) -> placeable.height },
    ) {
        var xPosition = 0

        placeables.forEach { (baseLine, placeable) ->
            placeable.placeRelative(x = xPosition, y = maxBaseLine - baseLine)

            xPosition += placeable.width
        }
    }
}
