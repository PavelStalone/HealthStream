package ru.health.stream.feature.chart.core

import androidx.compose.ui.graphics.drawscope.DrawScope

interface AxisDrawScope : DrawScope {

}

class AxisDrawScopeImpl(
    private val drawScope: DrawScope,
) : AxisDrawScope, DrawScope by drawScope {
}
