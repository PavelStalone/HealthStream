package ru.health.stream.feature.chart.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle

interface ChartDrawScope : DrawScope {

    val widthRange: ClosedFloatingPointRange<Float>
    val heightRange: ClosedFloatingPointRange<Float>

    val Float.xChart: Float
    val Float.yChart: Float
}

internal class ChartDrawScopeImpl(
    private val drawScope: DrawScope,
    override val widthRange: ClosedFloatingPointRange<Float>,
    override val heightRange: ClosedFloatingPointRange<Float>,
) : ChartDrawScope, DrawScope by drawScope {

    private val width = widthRange.endInclusive - widthRange.start
    private val height = heightRange.endInclusive - heightRange.start

    private val xKoef = (size.width / width)
    private val yKoef = (size.height / height)

    override val Float.xChart: Float get() = (this - widthRange.start) * xKoef
    override val Float.yChart: Float get() = (this - heightRange.start) * yKoef

    override fun drawCircle(
        color: Color,
        radius: Float,
        center: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawScope.drawCircle(
            color = color,
            radius = radius,
            center = Offset(
                x = center.x * (size.width / widthRange.endInclusive),
                y = center.y * (size.height / heightRange.endInclusive * -1f)
            ),
            alpha,
            style,
            colorFilter,
            blendMode
        )
    }

    override fun drawPath(
        path: Path,
        color: Color,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val matrix = Matrix().apply {
            reset()
            translate(y = size.height)
            scale(
                x = 1f,
                y = -1f
            )
        }

        path.copy().also { path ->
            path.transform(matrix)
            drawScope.drawPath(path, color, alpha, style, colorFilter, blendMode)
        }
    }

    override fun drawPath(
        path: Path,
        brush: Brush,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val matrix = Matrix().apply {
            reset()
            translate(y = size.height)
            scale(
                x = 1f,
                y = -1f
            )
        }

        path.copy().also { path ->
            path.transform(matrix)
            drawScope.drawPath(path, brush, alpha, style, colorFilter, blendMode)
        }
    }
}
