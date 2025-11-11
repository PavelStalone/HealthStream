package ru.health.stream.feature.chart.core

import androidx.compose.ui.graphics.drawscope.DrawScope

class HorizontalAxisLine(
    transform: (value: Float) -> String,
): AxisDrawable {

    override fun ChartDrawScope.draw(
        axis: Float,
        interpolator: Float
    ) {

    }
}
