package ru.health.stream.core.chart.core.drawable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.health.stream.core.chart.core.ChartDrawScope
import ru.health.stream.core.chart.core.Drawable

class GridLines(
    private val values: List<Float>,
    private val color: Color = Color.LightGray,
    private val style: DrawStyle = Stroke(width = 2f),
) : Drawable {

    override fun ChartDrawScope.draw(interpolator: Float) {
        values.forEach { y ->
            val path = Path().apply {
                moveTo(0f, y.yChart)
                lineTo(size.width, y.yChart)
            }
            drawPath(path = path, color = color, style = style)
        }
    }
}
