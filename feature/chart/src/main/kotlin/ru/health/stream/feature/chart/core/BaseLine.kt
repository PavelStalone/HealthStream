package ru.health.stream.feature.chart.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.health.stream.feature.chart.model.ChartPosition

class Line(
    points: List<ChartPosition.Point>
) : Drawable {

    private val sortedPoints = points.sortedBy { it.x }

    override val yRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        minOf { point -> point.y }..maxOf { point -> point.y }
    }
    override val xRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        first().x..last().x
    }

    init {
        require(sortedPoints.size >= 2) { "Line cant have been less 2 points" }
    }

    private fun createPath(interpolator: Float): Path = Path().apply {
        sortedPoints.first().let { point -> moveTo(x = point.x, y = point.y * interpolator) }
        sortedPoints.drop(1).forEach { point -> lineTo(x = point.x, y = point.y * interpolator) }
    }

    override fun DrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator), color = Color.Red, style = Stroke(width = 20f))
    }
}

class CubicLine(
    points: List<ChartPosition.Point>
) : Drawable {

    private val sortedPoints = points.sortedBy { it.x }

    override val yRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        minOf { point -> point.y }..maxOf { point -> point.y }
    }
    override val xRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        first().x..last().x
    }

    init {
        require(points.size >= 2) { "Line cant have been less 2 points" }
    }

    private fun createPath(interpolator: Float): Path = Path().apply {
        sortedPoints.first().let { point -> moveTo(x = point.x, y = point.y * interpolator) }
        sortedPoints.zipWithNext { lastPoint, point ->
            val xCenter = (point.x - lastPoint.x) / 2f

            cubicTo(
                x1 = lastPoint.x + xCenter, y1 = lastPoint.y * interpolator,
                x2 = lastPoint.x + xCenter, y2 = point.y * interpolator,
                x3 = point.x, y3 = point.y * interpolator,
            )
        }
    }

    override fun DrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator), color = Color.Blue, style = Stroke(width = 20f))
    }
}
