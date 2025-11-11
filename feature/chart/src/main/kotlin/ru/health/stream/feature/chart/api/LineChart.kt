package ru.health.stream.feature.chart.api

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import ru.health.stream.feature.chart.core.AxisDrawable
import ru.health.stream.feature.chart.core.CubicLine
import ru.health.stream.feature.chart.core.Drawable
import ru.health.stream.feature.chart.core.Line
import ru.health.stream.feature.chart.model.ChartPosition
import kotlin.math.max
import kotlin.math.min

@Composable
fun LineChart(
    lines: List<Drawable>,
    horizontalLine: AxisDrawable,
    verticalLine: AxisDrawable,
    modifier: Modifier = Modifier,
    startInitialAnimation: Boolean = true,
) {
    var animationStarted by remember { mutableStateOf(!startInitialAnimation) }

    val animatedY = List(lines.size) { index ->
        animateFloatAsState(
            targetValue = if (animationStarted) 1f else 0f,
            animationSpec = tween(
                durationMillis = 600 + (lines.size - index) * 100,
                delayMillis = index * 100,
            ),
            label = "line animation",
        )
    }

    LaunchedEffect(Unit) {
        delay(500)
        animationStarted = true
    }

    val infinite = rememberInfiniteTransition()
    val anim by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
    )

    lines.map { it.yRange }
        .reduce { acc, range ->
            min(acc.start, range.start)..max(acc.endInclusive, range.endInclusive)
        }
    val widthRange = remember(lines) {
        mutableStateOf(
            lines.map { it.xRange }
                .reduce { acc, range ->
                    min(acc.start, range.start)..max(acc.endInclusive, range.endInclusive)
                }
        )
    }
    val heightRange = remember(lines) {
        mutableStateOf(
            lines.map { it.yRange }
                .reduce { acc, range ->
                    min(acc.start, range.start)..max(acc.endInclusive, range.endInclusive)
                }
        )
    }

    Canvas(modifier = modifier) {
        ChartDrawScope(
            drawScope = this,
            widthRange = widthRange,
            heightRange = heightRange,
        ).run {

            lines.mapIndexed { index, drawablePath ->
                with(drawablePath) {
                    draw(anim)
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
            val width by rememberInfiniteTransition().animateFloat(
                initialValue = 1f, targetValue = 100f, infiniteRepeatable(
                    tween(5000), RepeatMode.Reverse
                )
            )
            LineChart(
                modifier = Modifier.fillMaxSize(),
                lines = listOf(
                    Line(
                        points = listOf(
                            ChartPosition.Point(x = 0f, y = 0f, z = 0f),
                            ChartPosition.Point(x = 1f, y = 40f, z = 0f),
                            ChartPosition.Point(x = 2f, y = 20f, z = 0f),
                            ChartPosition.Point(x = 3f, y = 50f, z = 0f),
                            ChartPosition.Point(x = 5f, y = 0f, z = 0f),
                        )
                    ),
                    CubicLine(
                        points = listOf(
                            ChartPosition.Point(x = 0f, y = 0f, z = 0f),
                            ChartPosition.Point(x = 2f, y = 40f, z = 0f),
                            ChartPosition.Point(x = 4f, y = 20f, z = 0f),
                            ChartPosition.Point(x = 6f, y = 50f, z = 0f),
                            ChartPosition.Point(x = width, y = 0f, z = 0f),
                        )
                    )
                )
            )
        }
    }
}
