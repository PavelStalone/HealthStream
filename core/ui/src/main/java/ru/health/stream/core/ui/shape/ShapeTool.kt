package ru.health.stream.core.ui.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
@ReadOnlyComposable
fun CornerBasedShape.multiShape(index: Int, count: Int): Shape {
    if (count == 1) {
        return this
    }

    return when (index) {
        0 -> copy(bottomStart = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
        count - 1 -> copy(topStart = CornerSize(0.0.dp), topEnd = CornerSize(0.0.dp))
        else -> RectangleShape
    }
}
