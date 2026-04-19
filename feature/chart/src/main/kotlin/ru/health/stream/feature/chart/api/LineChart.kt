package ru.health.stream.feature.chart.api

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastRoundToInt
import ru.health.stream.feature.chart.core.ChartDrawScopeImpl
import ru.health.stream.feature.chart.core.ChartScope
import ru.health.stream.feature.chart.core.ChartScopeInstance
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.XAxisSide
import ru.health.stream.feature.chart.core.YAxisSide
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.modifier.PointDataNode
import ru.health.stream.feature.chart.core.modifier.XAxisDataNode
import ru.health.stream.feature.chart.core.modifier.YAxisDataNode
import ru.health.stream.feature.chart.model.ChartPosition
import java.lang.Math.random
import kotlin.math.max

@Composable
fun LineChart(
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    animation: Boolean = true,
    chartDrawables: List<Drawable> = emptyList(),
    chartContent: @Composable ChartScope.() -> Unit = {},
) {
    val anim = remember(animation) { Animatable(initialValue = 0f.takeIf { animation } ?: 1f) }

    LaunchedEffect(animation) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(2000),
        )
    }

    rememberTextMeasurer()

    Layout(
        modifier = modifier,
        content = {
            ChartScopeInstance.chartContent()

            Canvas(modifier = Modifier.fillMaxSize()) {
                ChartDrawScopeImpl(
                    drawScope = this,
                    widthRange = xRange,
                    heightRange = yRange,
                ).run {
                    chartDrawables.forEach { chart ->
                        chart.run { draw(anim.value) }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val chartConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Data node extraction
        val nodes = measurables.groupBy { measurable ->
            when (measurable.parentData) {
                is XAxisDataNode -> Node.XAxisNode
                is YAxisDataNode -> Node.YAxisNode
                is PointDataNode -> Node.PointNode
                else -> Node.Chart
            }
        }

        // Paddings for plot area
        var topPadding = 0
        var leftPadding = 0
        var rightPadding = 0
        var bottomPadding = 0

        //region Data node preparing
        val xAxisPlaceables = nodes[Node.XAxisNode].orEmpty().map { measurable ->
            val placeable = measurable.measure(chartConstraints)
            val node = placeable.parentData as XAxisDataNode

            // Calculate max paddings for plot area
            when (node.side) {
                XAxisSide.Top -> topPadding = max(topPadding, placeable.height)
                XAxisSide.Bottom -> bottomPadding = max(bottomPadding, placeable.height)
            }

            node to placeable
        }
        val yAxisPlaceables = nodes[Node.YAxisNode].orEmpty().map { measurable ->
            val placeable = measurable.measure(chartConstraints)
            val node = placeable.parentData as YAxisDataNode

            when (node.side) {
                YAxisSide.Left -> leftPadding = max(leftPadding, placeable.width)
                YAxisSide.Right -> rightPadding = max(rightPadding, placeable.width)
            }

            node to placeable
        }
        val pointDataPlaceables = nodes[Node.PointNode].orEmpty().map { measurable ->
            val placeable = measurable.measure(chartConstraints)
            val node = placeable.parentData as PointDataNode

            node to placeable
        }
        //endregion

        // Calculate plot size area
        val plotWidth = constraints.maxWidth - (leftPadding + rightPadding)
        val plotHeight = constraints.maxHeight - (topPadding + bottomPadding)
        val plotConstraints = Constraints.fixed(width = plotWidth, height = plotHeight)

        // Preparing plot
        val plotPlaceable = nodes[Node.Chart].orEmpty().map { measurable ->
            measurable.measure(plotConstraints)
        }

        //region Map the relative coordinate to absolute in plot area
        fun mapToPixelX(x: Float): Int {
            if (!x.isFinite() || x.isNaN()) return 0

            val range = xRange.endInclusive - xRange.start
            val ratio = (x - xRange.start) / range
            return (ratio * plotWidth).fastRoundToInt()
        }

        fun mapToPixelY(y: Float): Int {
            if (!y.isFinite() || y.isNaN()) return plotHeight

            val range = yRange.endInclusive - yRange.start
            val ratio = (y - yRange.start) / range
            return (plotHeight - (ratio * plotHeight)).fastRoundToInt()
        }
        //endregion

        // Place content
        layout(constraints.maxWidth, constraints.maxHeight) {
            plotPlaceable.forEach { placeable -> placeable.place(x = leftPadding, y = topPadding) }

            // Place composable components in plot area
            pointDataPlaceables.forEach { (node, placeable) ->
                val xPos = leftPadding + mapToPixelX(node.x) - placeable.width
                val yPos = topPadding + mapToPixelY(node.y) - placeable.height

                val alignOffset = node.alignment.align(
                    size = IntSize(placeable.width, placeable.height),
                    space = IntSize(placeable.width * 2, placeable.height * 2),
                    layoutDirection = layoutDirection
                )

                placeable.place(IntOffset(x = xPos, y = yPos) + alignOffset)
            }

            //region Place composable components in axis area
            xAxisPlaceables.forEach { (node, placeable) ->
                val xPos = leftPadding + mapToPixelX(node.x) - placeable.width
                val yPos = when (node.side) {
                    XAxisSide.Bottom -> constraints.maxHeight - placeable.height
                    XAxisSide.Top -> 0
                }

                val alignOffset = node.alignment.align(
                    size = placeable.width,
                    space = placeable.width * 2,
                    layoutDirection = layoutDirection
                )

                placeable.place(x = xPos + alignOffset, y = yPos)
            }
            yAxisPlaceables.forEach { (node, placeable) ->
                val xPos = when (node.side) {
                    YAxisSide.Left -> 0
                    YAxisSide.Right -> constraints.maxWidth - placeable.width
                }
                val yPos = topPadding + mapToPixelY(node.y) - placeable.height

                val alignOffset = node.alignment.align(
                    size = placeable.height,
                    space = placeable.height * 2,
                )

                placeable.place(x = xPos, y = yPos + alignOffset)
            }
            //endregion
        }
    }
}

private enum class Node {

    Chart,
    XAxisNode,
    YAxisNode,
    PointNode,
}

@Preview
@Composable
fun PreviewLineChart() {
    val points = List(11) { index ->
        ChartPosition.Point(
            x = index.toFloat(),
            y = 40 + random().toFloat() * 50,
        )
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Gray)
        ) {
            LineChart(
                modifier = Modifier.fillMaxSize(),
                xRange = 0f..10f,
                yRange = 40f..90f,
                chartDrawables = listOf(
                    CubicLine(
                        points = points,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ),
                chartContent = {
                    points.forEach { point ->
                        Text(
                            modifier = Modifier.bindPoint(
                                x = point.x,
                                y = point.y,
                                alignment = if (point.y <= 65f) Alignment.BottomCenter else Alignment.TopCenter
                            ),
                            text = "x=${point.x.toInt()} y=${point.y.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            )
        }
    }
}
