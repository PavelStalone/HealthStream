package ru.health.stream.core.chart.core.drawable

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import ru.health.stream.core.chart.core.ChartDrawScope
import ru.health.stream.core.chart.core.Drawable
import ru.health.stream.core.chart.model.ChartPosition

class Scatter(
    private val positions: List<ChartPosition>,
    private val pointColor: Color,
    private val rangeColor: Color,
    private val radiusPoint: Dp,
) : Drawable {

    override fun ChartDrawScope.draw(interpolator: Float) {
        val radiusPx = radiusPoint.toPx()

        positions.forEach { position ->
            when (position) {
                is ChartPosition.Point -> {
                    drawPoint(position.x.xChart, position.y.yChart * interpolator, radiusPx)
                }

                is ChartPosition.Range.Vertical -> {
                    drawRange(
                        x1 = position.x.xChart,
                        y1 = position.bottom.yChart * interpolator,
                        x2 = position.x.xChart,
                        y2 = position.top.yChart * interpolator,
                        radiusPx = radiusPx
                    )
                }

                is ChartPosition.Range.Horizontal -> {
                    drawRange(
                        x1 = position.start.xChart,
                        y1 = position.y.yChart * interpolator,
                        x2 = position.end.xChart,
                        y2 = position.y.yChart * interpolator,
                        radiusPx = radiusPx
                    )
                }
            }
        }
    }

    private fun ChartDrawScope.drawPoint(x: Float, y: Float, radiusPx: Float) {
        val path = Path().apply {
            addOval(
                Rect(
                    left = x - radiusPx,
                    top = y - radiusPx,
                    right = x + radiusPx,
                    bottom = y + radiusPx
                )
            )
        }
        drawPath(path, color = pointColor, style = Fill)
    }

    private fun ChartDrawScope.drawRange(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        radiusPx: Float
    ) {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
        }
        drawPath(path, color = rangeColor, style = Stroke(width = radiusPx * 2f))

        drawPoint(x1, y1, radiusPx)
        drawPoint(x2, y2, radiusPx)
    }
}
