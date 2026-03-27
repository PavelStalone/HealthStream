package ru.health.stream.feature.chart.core.drawable

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import ru.health.stream.feature.chart.core.ChartDrawScope
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.model.ChartPosition

class CubicArea(
    private val brush: Brush,
    private val alpha: Float = 1f,
    points: List<ChartPosition.Point>,
) : Drawable {

    private val sortedPoints = points.sortedBy { point -> point.x }

    private fun ChartDrawScope.createPath(interpolator: Float): Path = Path().apply {
        val firstPoint = sortedPoints.first()
        val lastPoint = sortedPoints.last()

        moveTo(x = firstPoint.x.xChart, y = 0f.yChart)
        lineTo(x = firstPoint.x.xChart, y = firstPoint.y.yChart * interpolator)

        sortedPoints.zipWithNext { last, current ->
            val xCenter = (current.x - last.x) / 2f

            cubicTo(
                x1 = (last.x + xCenter).xChart, y1 = last.y.yChart * interpolator,
                x2 = (last.x + xCenter).xChart, y2 = current.y.yChart * interpolator,
                x3 = current.x.xChart, y3 = current.y.yChart * interpolator,
            )
        }

        lineTo(x = lastPoint.x.xChart, y = 0f.yChart)
        close()
    }

    override fun ChartDrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator = interpolator), brush = brush, alpha = alpha)
    }
}
