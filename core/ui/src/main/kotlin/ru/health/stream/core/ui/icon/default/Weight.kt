package ru.health.stream.core.ui.icon.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons

val Icons.Default.Weight: ImageVector
    get() {
        if (_Weight != null) {
            return _Weight!!
        }
        _Weight = ImageVector.Builder(
            name = "Default.Weight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(6f, 19f)
                horizontalLineTo(18f)
                lineTo(16.575f, 9f)
                horizontalLineTo(7.425f)
                lineTo(6f, 19f)
                close()
                moveTo(12f, 7f)
                curveTo(12.283f, 7f, 12.521f, 6.904f, 12.713f, 6.712f)
                curveTo(12.905f, 6.52f, 13.001f, 6.283f, 13f, 6f)
                curveTo(12.999f, 5.717f, 12.903f, 5.48f, 12.712f, 5.288f)
                curveTo(12.521f, 5.096f, 12.283f, 5f, 12f, 5f)
                curveTo(11.717f, 5f, 11.479f, 5.096f, 11.288f, 5.288f)
                curveTo(11.097f, 5.48f, 11.001f, 5.717f, 11f, 6f)
                curveTo(10.999f, 6.283f, 11.095f, 6.52f, 11.288f, 6.713f)
                curveTo(11.481f, 6.906f, 11.718f, 7.001f, 12f, 7f)
                close()
                moveTo(14.825f, 7f)
                horizontalLineTo(16.575f)
                curveTo(17.075f, 7f, 17.508f, 7.167f, 17.875f, 7.5f)
                curveTo(18.242f, 7.833f, 18.467f, 8.242f, 18.55f, 8.725f)
                lineTo(19.975f, 18.725f)
                curveTo(20.058f, 19.325f, 19.904f, 19.854f, 19.513f, 20.313f)
                curveTo(19.122f, 20.772f, 18.617f, 21.001f, 18f, 21f)
                horizontalLineTo(6f)
                curveTo(5.383f, 21f, 4.879f, 20.771f, 4.487f, 20.313f)
                curveTo(4.095f, 19.855f, 3.941f, 19.326f, 4.025f, 18.725f)
                lineTo(5.45f, 8.725f)
                curveTo(5.533f, 8.242f, 5.758f, 7.833f, 6.125f, 7.5f)
                curveTo(6.492f, 7.167f, 6.925f, 7f, 7.425f, 7f)
                horizontalLineTo(9.175f)
                curveTo(9.125f, 6.833f, 9.083f, 6.671f, 9.05f, 6.513f)
                curveTo(9.017f, 6.355f, 9f, 6.184f, 9f, 6f)
                curveTo(9f, 5.167f, 9.292f, 4.458f, 9.875f, 3.875f)
                curveTo(10.458f, 3.292f, 11.167f, 3f, 12f, 3f)
                curveTo(12.833f, 3f, 13.542f, 3.292f, 14.125f, 3.875f)
                curveTo(14.708f, 4.458f, 15f, 5.167f, 15f, 6f)
                curveTo(15f, 6.183f, 14.983f, 6.354f, 14.95f, 6.513f)
                curveTo(14.917f, 6.672f, 14.875f, 6.834f, 14.825f, 7f)
                close()
            }
        }.build()

        return _Weight!!
    }

@Suppress("ObjectPropertyName")
private var _Weight: ImageVector? = null
