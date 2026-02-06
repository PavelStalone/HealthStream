package ru.health.stream.feature.chart.core

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.debugInspectorInfo
import ru.health.stream.feature.chart.core.modifier.PointDataElement
import ru.health.stream.feature.chart.core.modifier.XAxisDataElement
import ru.health.stream.feature.chart.core.modifier.YAxisDataElement

@Immutable
@LayoutScopeMarker
interface ChartScope : PlotScope, AxisScope

@Immutable
@LayoutScopeMarker
interface PlotScope {

    @Stable
    fun Modifier.bindPoint(
        x: Float = Float.NaN,
        y: Float = Float.NaN,
        alignment: Alignment = Alignment.Center,
    ): Modifier
}

@Immutable
@LayoutScopeMarker
interface AxisScope {

    @Stable
    fun Modifier.bindXAxis(
        x: Float = Float.NaN,
        side: XAxisSide = XAxisSide.Bottom,
        alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    ): Modifier

    @Stable
    fun Modifier.bindYAxis(
        y: Float = Float.NaN,
        side: YAxisSide = YAxisSide.Left,
        alignment: Alignment.Vertical = Alignment.CenterVertically,
    ): Modifier
}

@Immutable
sealed interface YAxisSide {

    data object Left : YAxisSide
    data object Right : YAxisSide
}

@Immutable
sealed interface XAxisSide {

    data object Top : XAxisSide
    data object Bottom : XAxisSide
}

internal object ChartScopeInstance : ChartScope {

    @Stable
    override fun Modifier.bindPoint(
        x: Float,
        y: Float,
        alignment: Alignment
    ): Modifier = then(
        PointDataElement(
            x = x,
            y = y,
            alignment = alignment,
            inspectorInfo = debugInspectorInfo {
                name = "bindPoint"
                properties["x"] = x
                properties["y"] = y
                properties["alignment"] = alignment
            }
        )
    )

    override fun Modifier.bindXAxis(
        x: Float,
        side: XAxisSide,
        alignment: Alignment.Horizontal
    ): Modifier = then(
        XAxisDataElement(
            x = x,
            side = side,
            alignment = alignment,
            inspectorInfo = debugInspectorInfo {
                name = "bindXAxis"
                properties["x"] = x
                properties["side"] = side
                properties["alignment"] = alignment
            }
        )
    )

    override fun Modifier.bindYAxis(
        y: Float,
        side: YAxisSide,
        alignment: Alignment.Vertical
    ): Modifier = then(
        YAxisDataElement(
            y = y,
            side = side,
            alignment = alignment,
            inspectorInfo = debugInspectorInfo {
                name = "bindYAxis"
                properties["y"] = y
                properties["side"] = side
                properties["alignment"] = alignment
            }
        )
    )
}
