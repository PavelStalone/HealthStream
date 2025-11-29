package ru.health.stream.feature.chart.core

interface Drawable {

    fun ChartDrawScope.draw(interpolator: Float)
}
