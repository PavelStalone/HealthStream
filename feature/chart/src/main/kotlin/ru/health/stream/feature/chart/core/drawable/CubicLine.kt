package ru.health.stream.feature.chart.core.drawable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.health.stream.feature.chart.core.ChartDrawScope
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.model.ChartPosition

class CubicLine(
    points: List<ChartPosition.Point>,
    private val color: Color = Color.White,
    private val style: DrawStyle = Stroke(width = 20f),
) : Drawable {

    private val sortedPoints = points.sortedBy { it.x }

    private fun ChartDrawScope.createPath(interpolator: Float): Path = Path().apply {
        sortedPoints.first()
            .let { point ->
                moveTo(x = point.x.xChart, y = point.y.yChart * interpolator)
                lineTo(x = point.x.xChart, y = point.y.yChart * interpolator)
            }
        sortedPoints.zipWithNext { lastPoint, point ->
            val xCenter = (point.x - lastPoint.x) / 2f

            cubicTo(
                x1 = (lastPoint.x + xCenter).xChart, y1 = lastPoint.y.yChart * interpolator,
                x2 = (lastPoint.x + xCenter).xChart, y2 = point.y.yChart * interpolator,
                x3 = point.x.xChart, y3 = point.y.yChart * interpolator,
            )
        }
    }

    override fun ChartDrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator), color = color, style = style)
    }
}
