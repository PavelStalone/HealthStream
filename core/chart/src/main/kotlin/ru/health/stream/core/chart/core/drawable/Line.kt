package ru.health.stream.core.chart.core.drawable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.health.stream.core.chart.core.ChartDrawScope
import ru.health.stream.core.chart.core.Drawable
import ru.health.stream.core.chart.model.ChartPosition

class Line(
    points: List<ChartPosition.Point>,
    private val color: Color = Color.White,
    private val style: DrawStyle = Stroke(width = 20f),
) : Drawable {

    private val sortedPoints = points.sortedBy { it.x }

    init {
        require(sortedPoints.size >= 2) { "Line cant have been less 2 points" }
    }

    private fun ChartDrawScope.createPath(interpolator: Float): Path = Path().apply {
        sortedPoints.first()
            .let { point -> moveTo(x = point.x.xChart, y = point.y.yChart * interpolator) }
        sortedPoints.drop(1)
            .forEach { point -> lineTo(x = point.x.xChart, y = point.y.yChart * interpolator) }
    }

    override fun ChartDrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator), color = color, style = style)
    }
}
