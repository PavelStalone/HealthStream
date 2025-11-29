package ru.health.stream.feature.chart.api

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import ru.health.stream.feature.chart.core.ChartDrawScopeImpl
import ru.health.stream.feature.chart.core.Drawable
import kotlin.math.max

@Immutable
@LayoutScopeMarker
interface DescriptionScope {

    @Stable
    fun Modifier.bindChartValue(
        xValue: Float = Float.NaN,
        yValue: Float = Float.NaN,
        alignment: Alignment = Alignment.Center,
    ): Modifier
}

internal object DescriptionScopeInstance : DescriptionScope {

    @Stable
    override fun Modifier.bindChartValue(
        xValue: Float,
        yValue: Float,
        alignment: Alignment
    ): Modifier = then(
        ChartDataElement(
            xValue = xValue,
            yValue = yValue,
            alignment = alignment,
            inspectorInfo = debugInspectorInfo {
                name = "bindChartValue"
                properties["xValue"] = xValue
                properties["yValue"] = yValue
                properties["alignment"] = alignment
            }
        )
    )
}

private val Measurable.chartDataNode: ChartDataNode?
    get() = parentData as? ChartDataNode

private data class ChartDataElement(
    val xValue: Float,
    val yValue: Float,
    val alignment: Alignment,
    val inspectorInfo: InspectorInfo.() -> Unit,
) : ModifierNodeElement<ChartDataNode>() {

    override fun create(): ChartDataNode = ChartDataNode(
        xValue = xValue,
        yValue = yValue,
        alignment = alignment,
    )

    override fun update(node: ChartDataNode) {
        node.xValue = xValue
        node.yValue = yValue
        node.alignment = alignment
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }
}

private class ChartDataNode(
    var xValue: Float,
    var yValue: Float,
    var alignment: Alignment,
) : ParentDataModifierNode, Modifier.Node() {

    override fun Density.modifyParentData(parentData: Any?) = this@ChartDataNode
}

private val LeftChartBoundOffset = HorizontalAlignmentLine(::max)
private val RightChartBoundOffset = HorizontalAlignmentLine(::max)
private val TopChartBoundOffset = HorizontalAlignmentLine(::max)
private val BottomChartBoundOffset = HorizontalAlignmentLine(::max)

@Composable
fun Chart(
    modifier: Modifier,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    chartDrawables: List<Drawable> = emptyList(),
    animation: Boolean = true,
    descriptionContent: @Composable DescriptionScope.() -> Unit,
) {
    val infinite = rememberInfiniteTransition()
    val anim by infinite.animateFloat(
        initialValue = 0f.takeIf { animation } ?: 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
    )

    Layout(
        modifier = modifier,
        content = {
            DescriptionScopeInstance.descriptionContent()

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue)
            ) {
                ChartDrawScopeImpl(
                    drawScope = this,
                    widthRange = xRange,
                    heightRange = yRange,
                ).run {
                    chartDrawables.map { chart ->
                        with(chart) { draw(anim) }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        var topLeftOffset = IntOffset(x = 0, y = 0)
        var bottomRightOffset = IntOffset(x = 0, y = 0)

        val descriptionsPlaceable = measurables.filter { it.parentData is ChartDataNode }
            .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val chartMeasurables = measurables.filterNot { it.parentData is ChartDataNode }

        descriptionsPlaceable
            .forEach {
                with(topLeftOffset) {
                    topLeftOffset = copy(
                        x = max(x, it[LeftChartBoundOffset]),
                        y = max(y, it[TopChartBoundOffset]),
                    )
                }
                with(bottomRightOffset) {
                    bottomRightOffset = copy(
                        x = max(x, it[RightChartBoundOffset]),
                        y = max(y, it[BottomChartBoundOffset]),
                    )
                }
            }

        val chartSize = IntSize(
            width = constraints.maxWidth - topLeftOffset.x - bottomRightOffset.x,
            height = constraints.maxHeight - topLeftOffset.y - bottomRightOffset.y,
        )

        val chartConstraints = Constraints.fixed(
            width = chartSize.width,
            height = chartSize.height,
        )

        val chartsPlaceable = chartMeasurables.map {
            it.measure(chartConstraints)
        }

        fun xConverter(xValue: Float): Int {
            val koef = chartSize.width / (xRange.endInclusive - xRange.start)

            if (xValue.isNaN()) return 0
            return ((xValue - xRange.start) * koef).fastRoundToInt()
        }

        fun yConverter(yValue: Float): Int {
            val koef = (chartSize.height / (yRange.endInclusive - yRange.start)) * -1f

            if (yValue.isNaN()) return chartSize.height
            return chartSize.height + ((yValue - yRange.start) * koef).fastRoundToInt()
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            chartsPlaceable.forEach { placeable ->
                placeable.place(topLeftOffset)
            }
            descriptionsPlaceable.forEach { placeable ->
                val chartNode = placeable.parentData as? ChartDataNode

                if (chartNode != null) {
                    val xPos = topLeftOffset.x + xConverter(chartNode.xValue)
                    val yPos = topLeftOffset.y + yConverter(chartNode.yValue)

                    val spaceWidth = placeable.width * 2
//                        min(placeable.width, min(constraints.maxWidth - xPos, xPos)) * 2
                    val spaceHeight = placeable.height * 2
//                        min(placeable.height, min(constraints.maxHeight - yPos, yPos)) * 2

                    val alignOffset = chartNode.alignment.align(
                        size = IntSize(0, 0),
                        space = IntSize(spaceWidth, spaceHeight),
                        layoutDirection = layoutDirection
                    )

                    placeable.place(
                        IntOffset(x = xPos, y = yPos) - IntOffset(
                            x = spaceWidth / 2,
                            y = spaceHeight / 2,
                        ) + (alignOffset / 2f)
                    )
                } else {
                    placeable.place(0, 0)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewChart() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Gray)
        ) {
            val transition = rememberInfiniteTransition()
            val anim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    tween(1000), RepeatMode.Reverse
                )
            )

            Chart(
                modifier = Modifier.fillMaxSize(),
                xRange = 0f..10f,
                yRange = 0f..10f,
                descriptionContent = {
                    Column(
                        Modifier
                            .bindChartValue(xValue = anim, alignment = Alignment.Center)
                            .background(Color.Green)
                            .padding(vertical = 20.dp)
                    ) {
                        Text(text = "-|-", style = TextStyle.Default.copy(fontSize = 30.sp))
                    }
                    Column(
                        Modifier
                            .bindChartValue(xValue = anim, alignment = Alignment.CenterStart)
                            .background(Color.Green)
                    ) {
                        Text(text = "-|-", style = TextStyle.Default.copy(fontSize = 30.sp))
                    }
                    Column(
                        Modifier
                            .bindChartValue(xValue = anim, alignment = Alignment.CenterEnd)
                            .background(Color.Green)
                    ) {
                        Text(text = "-|-", style = TextStyle.Default.copy(fontSize = 30.sp))
                    }
                }
            )
        }
    }
}
