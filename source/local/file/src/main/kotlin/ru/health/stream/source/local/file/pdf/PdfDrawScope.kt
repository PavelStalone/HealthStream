package ru.health.stream.source.local.file.pdf

import android.graphics.Bitmap
import android.graphics.PathMeasure
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.DrawTransform
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import ru.health.stream.feature.chart.model.path.DashPathEffect
import java.io.ByteArrayOutputStream

class PdfDrawScope(
    val pageSize: Size,
    val pdfCanvas: PdfCanvas,
    val verticalMargin: Offset = Offset(0f, 0f), // bottom, top
    val horizontalMargin: Offset = Offset(0f, 0f), // start, end
) : DrawScope {

    private val planeSize = Size(
        width = pageSize.width - horizontalMargin.run { x + y },
        height = pageSize.height - verticalMargin.y,
    )

    override val drawContext: DrawContext = object : DrawContext {

        private val context = this

        override var size: Size = planeSize

        override val transform: DrawTransform = object : DrawTransform {
            override var size: Size = context.size

            override fun inset(
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ) {
                translate(left, top)
                size = Size(
                    width = size.width - (left + right),
                    height = size.height - (top + bottom)
                )
            }

            override fun clipRect(
                left: Float,
                top: Float,
                right: Float,
                bottom: Float,
                clipOp: ClipOp
            ) {
                with(this@PdfDrawScope) {
                    pdfCanvas.rectangle(
                        (horizontalMargin.x + left).toDouble(),
                        (verticalMargin.x + top).toDouble(),
                        (right - left).toDouble(),
                        (bottom - top).toDouble()
                    )
                    pdfCanvas.clip()
                    pdfCanvas.endPath()
                }
            }

            override fun clipPath(
                path: Path,
                clipOp: ClipOp
            ) {
                TODO("Not yet implemented")
            }

            override fun translate(left: Float, top: Float) {
                this@PdfDrawScope.pdfCanvas.concatMatrix(
                    1.0,
                    0.0,
                    0.0,
                    1.0,
                    left.toDouble(),
                    -top.toDouble()
                )
            }

            override fun rotate(degrees: Float, pivot: Offset) {
                val radians = Math.toRadians(degrees.toDouble())
                val cos = Math.cos(radians)
                val sin = Math.sin(radians)

                // Сдвиг к точке вращения, поворот, сдвиг обратно
                translate(pivot.x, pivot.y)
                this@PdfDrawScope.pdfCanvas.concatMatrix(cos, sin, -sin, cos, 0.0, 0.0)
                translate(-pivot.x, -pivot.y)
            }

            override fun scale(
                scaleX: Float,
                scaleY: Float,
                pivot: Offset
            ) {
                translate(pivot.x, pivot.y)
                this@PdfDrawScope.pdfCanvas.concatMatrix(
                    scaleX.toDouble(),
                    0.0,
                    0.0,
                    scaleY.toDouble(),
                    0.0,
                    0.0
                )
                translate(-pivot.x, -pivot.y)
            }

            override fun transform(matrix: Matrix) {
                val values = matrix.values
                // Matrix в Compose 4x4, PdfCanvas ждет 3x3 Affine (a, b, c, d, e, f)
                this@PdfDrawScope.pdfCanvas.concatMatrix(
                    values[Matrix.ScaleX].toDouble(),
                    values[Matrix.SkewY].toDouble(),
                    values[Matrix.SkewX].toDouble(),
                    values[Matrix.ScaleY].toDouble(),
                    values[Matrix.TranslateX].toDouble(),
                    values[Matrix.TranslateY].toDouble()
                )
            }
        }
    }

    override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
    override val density: Float = drawContext.density.density
    override val fontScale: Float = 1f

    fun drawText(
        text: String,
        font: PdfFont,
        offset: Offset,
        fontSize: Float = 12f,
        color: Color = Color.Black
    ) {
        withAlpha(color.alpha) {
            pdfCanvas.beginText()
            pdfCanvas.setFontAndSize(font, fontSize)
            pdfCanvas.setFillColor(color.asPdf())

            val x = (horizontalMargin.x + offset.x).toDouble()
            val y = (verticalMargin.x + offset.y).toDouble()

            pdfCanvas.moveText(x, y)
            pdfCanvas.showText(text)
            pdfCanvas.endText()
        }
    }

    override fun drawLine(
        brush: Brush,
        start: Offset,
        end: Offset,
        strokeWidth: Float,
        cap: StrokeCap,
        pathEffect: PathEffect?,
        alpha: Float,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawLine(
                color = brush.value,
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = cap,
                pathEffect = pathEffect,
                alpha = alpha,
                colorFilter = colorFilter,
                blendMode = blendMode
            )
        }
    }

    override fun drawLine(
        color: Color,
        start: Offset,
        end: Offset,
        strokeWidth: Float,
        cap: StrokeCap,
        pathEffect: PathEffect?,
        alpha: Float,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        withAlpha(alpha = color.alpha) {
            if (pathEffect is DashPathEffect) {
                pdfCanvas.setLineDash(pathEffect.intervals, pathEffect.phase)
            }

            pdfCanvas.setStrokeColor(color.asPdf())
            pdfCanvas.setLineWidth(strokeWidth)
            pdfCanvas.setLineCapStyle(cap.asPdf())
            pdfCanvas.moveTo(
                (horizontalMargin.x + start.x).toDouble(),
                (verticalMargin.x + start.y).toDouble()
            )
            pdfCanvas.lineTo(
                (horizontalMargin.x + end.x).toDouble(),
                (verticalMargin.x + end.y).toDouble()
            )
            pdfCanvas.stroke()
            pdfCanvas.setLineDash(0f)
        }
    }

    override fun drawRect(
        brush: Brush,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawRect(
                color = brush.value,
                topLeft = topLeft,
                size = size,
                alpha = alpha,
                style = style,
                colorFilter = colorFilter,
                blendMode = blendMode
            )
        }
    }

    override fun drawRect(
        color: Color,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        withAlpha(alpha = color.alpha) {
            val x = (horizontalMargin.x + topLeft.x).toDouble()
            val y = (verticalMargin.x + topLeft.y - size.height).toDouble()
            val w = size.width.toDouble()
            val h = size.height.toDouble()

            when (style) {
                Fill -> {
                    pdfCanvas.setFillColor(color.asPdf())
                    pdfCanvas.rectangle(x, y, w, h)
                    pdfCanvas.fill()
                }

                is Stroke -> {
                    pdfCanvas.setStrokeColor(color.asPdf())
                    pdfCanvas.setLineWidth(style.width)
                    pdfCanvas.rectangle(x, y, w, h)
                    pdfCanvas.stroke()
                }
            }
        }
    }

    override fun drawImage(
        image: ImageBitmap,
        topLeft: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val bitmap = image.asAndroidBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageData = ImageDataFactory.create(stream.toByteArray())

        val x = (horizontalMargin.x + topLeft.x).toDouble()
        val y = (verticalMargin.x + topLeft.y).toDouble()
        val w = image.width.toDouble()
        val h = image.height.toDouble()

        pdfCanvas.addImageFittedIntoRectangle(
            imageData,
            Rectangle(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()),
            false
        )
    }

    override fun drawImage(
        image: ImageBitmap,
        srcOffset: IntOffset,
        srcSize: IntSize,
        dstOffset: IntOffset,
        dstSize: IntSize,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val bitmap = image.asAndroidBitmap()
        val finalBitmap =
            if (srcOffset != IntOffset.Zero || srcSize != IntSize(image.width, image.height)) {
                Bitmap.createBitmap(bitmap, srcOffset.x, srcOffset.y, srcSize.width, srcSize.height)
            } else {
                bitmap
            }

        val stream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageData = ImageDataFactory.create(stream.toByteArray())

        val x = (horizontalMargin.x + dstOffset.x).toDouble()
        val y = (verticalMargin.x + dstOffset.y).toDouble()
        val w = dstSize.width.toDouble()
        val h = dstSize.height.toDouble()

        pdfCanvas.addImageFittedIntoRectangle(
            imageData,
            Rectangle(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()),
            false
        )
    }

    override fun drawRoundRect(
        brush: Brush,
        topLeft: Offset,
        size: Size,
        cornerRadius: CornerRadius,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawRoundRect(
                brush.value,
                topLeft,
                size,
                cornerRadius,
                style,
                alpha,
                colorFilter,
                blendMode
            )
        }
    }

    override fun drawRoundRect(
        color: Color,
        topLeft: Offset,
        size: Size,
        cornerRadius: CornerRadius,
        style: DrawStyle,
        alpha: Float,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val x = (horizontalMargin.x + topLeft.x).toDouble()
        val y = (verticalMargin.x + topLeft.y).toDouble()
        val w = size.width.toDouble()
        val h = size.height.toDouble()
        val r = cornerRadius.x.toDouble()

        when (style) {
            Fill -> {
                pdfCanvas.setFillColor(color.asPdf())
                pdfCanvas.roundRectangle(x, y, w, h, r)
                pdfCanvas.fill()
            }

            is Stroke -> {
                pdfCanvas.setStrokeColor(color.asPdf())
                pdfCanvas.setLineWidth(style.width)
                pdfCanvas.roundRectangle(x, y, w, h, r)
                pdfCanvas.stroke()
            }
        }
    }

    override fun drawCircle(
        brush: Brush,
        radius: Float,
        center: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawCircle(brush.value, radius, center, alpha, style, colorFilter, blendMode)
        }
    }

    override fun drawCircle(
        color: Color,
        radius: Float,
        center: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val x = (horizontalMargin.x + center.x).toDouble()
        val y = (verticalMargin.x + center.y).toDouble()

        withAlpha(alpha = color.alpha) {
            when (style) {
                Fill -> {
                    pdfCanvas.setFillColor(color.asPdf())
                    pdfCanvas.circle(x, y, radius.toDouble())
                    pdfCanvas.fill()
                }

                is Stroke -> {
                    pdfCanvas.setStrokeColor(color.asPdf())
                    pdfCanvas.setLineWidth(style.width)
                    pdfCanvas.circle(x, y, radius.toDouble())
                    pdfCanvas.stroke()
                }
            }
        }
    }

    override fun drawOval(
        brush: Brush,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawOval(brush.value, topLeft, size, alpha, style, colorFilter, blendMode)
        }
    }

    override fun drawOval(
        color: Color,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val x1 = (horizontalMargin.x + topLeft.x).toDouble()
        val y1 = (verticalMargin.x + topLeft.y).toDouble()
        val x2 = x1 + size.width
        val y2 = y1 + size.height

        when (style) {
            Fill -> {
                pdfCanvas.setFillColor(color.asPdf())
                pdfCanvas.ellipse(x1, y1, x2, y2)
                pdfCanvas.fill()
            }

            is Stroke -> {
                pdfCanvas.setStrokeColor(color.asPdf())
                pdfCanvas.setLineWidth(style.width)
                pdfCanvas.ellipse(x1, y1, x2, y2)
                pdfCanvas.stroke()
            }
        }
    }

    override fun drawArc(
        brush: Brush,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawArc(
                brush.value,
                startAngle,
                sweepAngle,
                useCenter,
                topLeft,
                size,
                alpha,
                style,
                colorFilter,
                blendMode
            )
        }
    }

    override fun drawArc(
        color: Color,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val x1 = (horizontalMargin.x + topLeft.x).toDouble()
        val y1 = (verticalMargin.x + topLeft.y).toDouble()
        val x2 = x1 + size.width
        val y2 = y1 + size.height

        if (useCenter) {
            val cx = x1 + size.width / 2
            val cy = y1 + size.height / 2
            pdfCanvas.moveTo(cx, cy)
        }

        pdfCanvas.arc(x1, y1, x2, y2, startAngle.toDouble(), sweepAngle.toDouble())

        if (useCenter) {
            pdfCanvas.lineTo(x1 + size.width / 2, y1 + size.height / 2)
        }

        when (style) {
            Fill -> {
                pdfCanvas.setFillColor(color.asPdf())
                pdfCanvas.fill()
            }

            is Stroke -> {
                pdfCanvas.setStrokeColor(color.asPdf())
                pdfCanvas.setLineWidth(style.width)
                pdfCanvas.stroke()
            }
        }
    }

    override fun drawPath(
        path: Path,
        color: Color,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        val androidPath = path.asAndroidPath()
        val pathMeasure = PathMeasure(androidPath, false)
        val pos = FloatArray(2)

        do {
            val length = pathMeasure.length
            if (length > 0f) {
                pathMeasure.getPosTan(0f, pos, null)
                pdfCanvas.moveTo(
                    (horizontalMargin.x + pos[0]).toDouble(),
                    (verticalMargin.x + pos[1]).toDouble()
                )

                var distance = 1f
                while (distance < length) {
                    pathMeasure.getPosTan(distance, pos, null)
                    pdfCanvas.lineTo(
                        (horizontalMargin.x + pos[0]).toDouble(),
                        (verticalMargin.x + pos[1]).toDouble()
                    )
                    distance += 1f
                }

                pathMeasure.getPosTan(length, pos, null)
                pdfCanvas.lineTo(
                    (horizontalMargin.x + pos[0]).toDouble(),
                    (verticalMargin.x + pos[1]).toDouble()
                )
            }
        } while (pathMeasure.nextContour())

        withAlpha(alpha = color.alpha) {
            when (style) {
                Fill -> {
                    pdfCanvas.setFillColor(color.asPdf())
                    pdfCanvas.fill()
                }

                is Stroke -> {
                    pdfCanvas.setStrokeColor(color.asPdf())
                    pdfCanvas.setLineWidth(style.width)
                    pdfCanvas.setLineCapStyle(style.cap.asPdf())

                    val pathEffect = style.pathEffect
                    if (pathEffect is DashPathEffect) {
                        pdfCanvas.setLineDash(pathEffect.intervals, pathEffect.phase)
                    }

                    pdfCanvas.stroke()
                    pdfCanvas.setLineDash(0f)
                }
            }
        }
    }

    override fun drawPath(
        path: Path,
        brush: Brush,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawPath(path, brush.value, alpha, style, colorFilter, blendMode)
        }
    }

    override fun drawPoints(
        points: List<Offset>,
        pointMode: PointMode,
        color: Color,
        strokeWidth: Float,
        cap: StrokeCap,
        pathEffect: PathEffect?,
        alpha: Float,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        pdfCanvas.setStrokeColor(color.asPdf())
        pdfCanvas.setLineWidth(strokeWidth)
        pdfCanvas.setLineCapStyle(cap.asPdf())

        if (pathEffect is DashPathEffect) {
            pdfCanvas.setLineDash(pathEffect.intervals, pathEffect.phase)
        }

        when (pointMode) {
            PointMode.Points -> {
                pdfCanvas.setFillColor(color.asPdf())
                points.forEach { point ->
                    pdfCanvas.circle(
                        (horizontalMargin.x + point.x).toDouble(),
                        (verticalMargin.x + point.y).toDouble(),
                        (strokeWidth / 2).toDouble()
                    )
                    pdfCanvas.fill()
                }
            }

            PointMode.Lines -> {
                for (i in 0 until points.size - 1 step 2) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    pdfCanvas.moveTo(
                        (horizontalMargin.x + p1.x).toDouble(),
                        (verticalMargin.x + p1.y).toDouble()
                    )
                    pdfCanvas.lineTo(
                        (horizontalMargin.x + p2.x).toDouble(),
                        (verticalMargin.x + p2.y).toDouble()
                    )
                }
                pdfCanvas.stroke()
            }

            PointMode.Polygon -> {
                if (points.isNotEmpty()) {
                    pdfCanvas.moveTo(
                        (horizontalMargin.x + points[0].x).toDouble(),
                        (verticalMargin.x + points[0].y).toDouble()
                    )
                    for (i in 1 until points.size) {
                        pdfCanvas.lineTo(
                            (horizontalMargin.x + points[i].x).toDouble(),
                            (verticalMargin.x + points[i].y).toDouble()
                        )
                    }
                }
                pdfCanvas.stroke()
            }
        }
        pdfCanvas.setLineDash(0f)
    }

    override fun drawPoints(
        points: List<Offset>,
        pointMode: PointMode,
        brush: Brush,
        strokeWidth: Float,
        cap: StrokeCap,
        pathEffect: PathEffect?,
        alpha: Float,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        if (brush is SolidColor) {
            drawPoints(
                points,
                pointMode,
                brush.value,
                strokeWidth,
                cap,
                pathEffect,
                alpha,
                colorFilter,
                blendMode
            )
        }
    }

    private fun Color.asPdf(): DeviceRgb = DeviceRgb(red, green, blue)

    private fun withAlpha(alpha: Float, block: () -> Unit) {
        if (alpha >= 1f) {
            block()
        } else {
            pdfCanvas.saveState()
            val gState = PdfExtGState().setFillOpacity(alpha)
                .setStrokeOpacity(alpha)

            pdfCanvas.setExtGState(gState)
            block()
            pdfCanvas.restoreState()
        }
    }

    private fun StrokeCap.asPdf(): Int = when (this) {
        StrokeCap.Butt -> PdfCanvasConstants.LineCapStyle.BUTT
        StrokeCap.Round -> PdfCanvasConstants.LineCapStyle.ROUND
        StrokeCap.Square -> PdfCanvasConstants.LineCapStyle.PROJECTING_SQUARE
        else -> PdfCanvasConstants.LineCapStyle.BUTT
    }
}

