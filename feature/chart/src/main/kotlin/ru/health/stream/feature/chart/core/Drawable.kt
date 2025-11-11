package ru.health.stream.feature.chart.core

import androidx.compose.ui.graphics.drawscope.DrawScope

interface Drawable {
    val yRange: ClosedFloatingPointRange<Float>
    val xRange: ClosedFloatingPointRange<Float>

    fun DrawScope.draw(interpolator: Float)
}

interface AxisDrawable {

    fun ChartDrawScope.draw(axis: Float, interpolator: Float)
}