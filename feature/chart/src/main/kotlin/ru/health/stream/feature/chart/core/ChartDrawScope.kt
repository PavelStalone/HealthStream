package ru.health.stream.feature.chart.core

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle

interface ChartDrawScope : DrawScope {

    val widthRange: ClosedFloatingPointRange<Float>
    val heightRange: ClosedFloatingPointRange<Float>
}

internal class ChartDrawScopeImpl(
    private val drawScope: DrawScope,
    widthRange: MutableState<ClosedFloatingPointRange<Float>>,
    heightRange: MutableState<ClosedFloatingPointRange<Float>>,
) : ChartDrawScope, DrawScope by drawScope {

    override var widthRange by widthRange
    override var heightRange by heightRange

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
                x = size.width / widthRange.endInclusive,
                y = size.height / heightRange.endInclusive * -1f
            )
        }

        path.transform(matrix)
        drawScope.drawPath(path, color, alpha, style, colorFilter, blendMode)
    }
}
