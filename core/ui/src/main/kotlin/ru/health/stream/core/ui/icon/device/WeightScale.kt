package ru.health.stream.core.ui.icon.device

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons

val Icons.Device.WeightScale: ImageVector
    get() {
        if (_WeightScale != null) {
            return _WeightScale!!
        }
        _WeightScale = ImageVector.Builder(
            name = "WeightScale",
            defaultWidth = 20.dp,
            defaultHeight = 22.dp,
            viewportWidth = 20f,
            viewportHeight = 22f
        ).apply {
            path(
                stroke = SolidColor(Color.Green),
                strokeLineWidth = 1.5f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.433f, 3.75f)
                curveTo(3.068f, 3.812f, 2.225f, 4.004f, 1.629f, 4.601f)
                curveTo(0.75f, 5.481f, 0.75f, 6.898f, 0.75f, 9.731f)
                verticalLineTo(14.74f)
                curveTo(0.75f, 17.573f, 0.75f, 18.99f, 1.629f, 19.87f)
                curveTo(2.507f, 20.75f, 3.922f, 20.75f, 6.75f, 20.75f)
                horizontalLineTo(12.75f)
                curveTo(15.578f, 20.75f, 16.993f, 20.75f, 17.871f, 19.87f)
                curveTo(18.749f, 18.99f, 18.75f, 17.573f, 18.75f, 14.74f)
                verticalLineTo(9.73f)
                curveTo(18.75f, 6.897f, 18.75f, 5.48f, 17.871f, 4.6f)
                curveTo(17.275f, 4.003f, 16.431f, 3.81f, 15.067f, 3.749f)
            }
            path(
                stroke = SolidColor(Color.Green),
                strokeLineWidth = 1.5f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.775f, 5.29f)
                curveTo(4.295f, 3.41f, 4.055f, 2.47f, 4.445f, 1.78f)
                curveTo(4.549f, 1.596f, 4.682f, 1.43f, 4.839f, 1.288f)
                curveTo(5.428f, 0.75f, 6.419f, 0.75f, 8.4f, 0.75f)
                horizontalLineTo(11.1f)
                curveTo(13.081f, 0.75f, 14.072f, 0.75f, 14.661f, 1.288f)
                curveTo(14.818f, 1.431f, 14.951f, 1.598f, 15.055f, 1.781f)
                curveTo(15.445f, 2.471f, 15.205f, 3.411f, 14.725f, 5.291f)
                curveTo(14.341f, 6.789f, 14.15f, 7.538f, 13.638f, 8.031f)
                curveTo(13.498f, 8.166f, 13.344f, 8.284f, 13.177f, 8.384f)
                curveTo(12.563f, 8.75f, 11.773f, 8.75f, 10.194f, 8.75f)
                horizontalLineTo(9.306f)
                curveTo(7.726f, 8.75f, 6.936f, 8.75f, 6.323f, 8.384f)
                curveTo(6.157f, 8.284f, 6.003f, 8.166f, 5.863f, 8.032f)
                curveTo(5.35f, 7.538f, 5.158f, 6.789f, 4.775f, 5.29f)
                close()
            }
            path(
                stroke = SolidColor(Color.Green),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.75f, 16.75f)
                horizontalLineTo(11.75f)
                moveTo(9.25f, 8.75f)
                lineTo(10.25f, 5.75f)
            }
        }.build()

        return _WeightScale!!
    }

@Suppress("ObjectPropertyName")
private var _WeightScale: ImageVector? = null
