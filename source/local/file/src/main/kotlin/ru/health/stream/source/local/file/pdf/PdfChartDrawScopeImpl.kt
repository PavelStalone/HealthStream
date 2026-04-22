package ru.health.stream.source.local.file.pdf

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import com.itextpdf.kernel.font.PdfFont
import ru.health.stream.feature.chart.core.ChartDrawScope

internal class PdfChartDrawScopeImpl(
    private val drawScope: PdfDrawScope,
    override val widthRange: ClosedFloatingPointRange<Float>,
    override val heightRange: ClosedFloatingPointRange<Float>,
) : ChartDrawScope, DrawScope by drawScope {

    private val width = widthRange.endInclusive - widthRange.start
    private val height = heightRange.endInclusive - heightRange.start

    private val xKoef = (size.width / width)
    private val yKoef = (size.height / height)

    override val Float.xChart: Float get() = (this - widthRange.start) * xKoef
    override val Float.yChart: Float get() = (this - heightRange.start) * yKoef

    fun drawText(
        text: String,
        font: PdfFont,
        offset: Offset,
        fontSize: Float = 12f,
        color: Color = Color.Black,
    ) = drawScope.drawText(
        text = text,
        font = font,
        color = color,
        offset = offset,
        fontSize = fontSize,
    )
}
