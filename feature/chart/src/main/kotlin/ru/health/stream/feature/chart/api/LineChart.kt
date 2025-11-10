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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.health.stream.feature.chart.model.ChartPosition
import kotlin.math.max
import kotlin.math.min

@Composable
fun LineChart(
    lines: List<Drawable>,
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

internal class ChartDrawScope(
    private val drawScope: DrawScope,
    widthRange: MutableState<ClosedFloatingPointRange<Float>>,
    heightRange: MutableState<ClosedFloatingPointRange<Float>>,
) : DrawScope by drawScope {
    private var widthRange by widthRange
    private var heightRange by heightRange

    override fun drawCircle(
        color: Color,
        radius: Float,
        center: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
//        if (center.x > widthRange.endInclusive) widthRange = widthRange.start..center.x
//        if (center.y > heightRange.endInclusive) heightRange = heightRange.start..center.y

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
        val bounds = path.getBounds()

//        if (bounds.width > widthRange.endInclusive) widthRange = widthRange.start..bounds.width
//        if (bounds.height > heightRange.endInclusive) heightRange = heightRange.start..bounds.height

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
//        Log.i("LineChart", "heightRange: $heightRange, class: $this")
    }
}

class Line(
    points: List<ChartPosition.Point>
) : Drawable {

    private val sortedPoints = points.sortedBy { it.x }

    override val yRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        minOf { point -> point.y }..maxOf { point -> point.y }
    }
    override val xRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        first().x..last().x
    }

    init {
        require(sortedPoints.size >= 2) { "Line cant have been less 2 points" }
    }

    private fun createPath(interpolator: Float): Path = Path().apply {
        sortedPoints.first().let { point -> moveTo(x = point.x, y = point.y * interpolator) }
        sortedPoints.drop(1).forEach { point -> lineTo(x = point.x, y = point.y * interpolator) }
    }

    override fun DrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator), color = Color.Red, style = Stroke(width = 20f))
    }
}

class CubicLine(
    points: List<ChartPosition.Point>
) : Drawable {

    private val sortedPoints = points.sortedBy { it.x }

    override val yRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        minOf { point -> point.y }..maxOf { point -> point.y }
    }
    override val xRange: ClosedFloatingPointRange<Float> = with(sortedPoints) {
        first().x..last().x
    }

    init {
        require(points.size >= 2) { "Line cant have been less 2 points" }
    }

    private fun createPath(interpolator: Float): Path = Path().apply {
        sortedPoints.first().let { point -> moveTo(x = point.x, y = point.y * interpolator) }
        sortedPoints.zipWithNext { lastPoint, point ->
            val xCenter = (point.x - lastPoint.x) / 2f

            cubicTo(
                x1 = lastPoint.x + xCenter, y1 = lastPoint.y * interpolator,
                x2 = lastPoint.x + xCenter, y2 = point.y * interpolator,
                x3 = point.x, y3 = point.y * interpolator,
            )
        }
    }

    override fun DrawScope.draw(interpolator: Float) {
        drawPath(path = createPath(interpolator), color = Color.Blue, style = Stroke(width = 20f))
    }
}

interface Drawable {
    val yRange: ClosedFloatingPointRange<Float>
    val xRange: ClosedFloatingPointRange<Float>

    fun DrawScope.draw(interpolator: Float)
}

var state by mutableStateOf(1f)

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
                            ChartPosition.Point(x = 1f, y = state, z = 0f),
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
