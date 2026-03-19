package ru.health.stream.core.ui.icon.device

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.DeviceIcons

val DeviceIcons.PulseOximeter: ImageVector
    get() {
        if (_PulseOximeter != null) {
            return _PulseOximeter!!
        }
        _PulseOximeter = ImageVector.Builder(
            name = "PulseOximeter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF36E881)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15f, 6f)
                horizontalLineTo(3.5f)
                curveTo(3.367f, 6f, 3.24f, 6.053f, 3.146f, 6.146f)
                curveTo(3.053f, 6.24f, 3f, 6.367f, 3f, 6.5f)
                verticalLineTo(8.925f)
                lineTo(3.618f, 9.774f)
                curveTo(3.866f, 10.116f, 4f, 10.527f, 4f, 10.95f)
                verticalLineTo(13.05f)
                curveTo(4f, 13.473f, 3.866f, 13.884f, 3.618f, 14.226f)
                lineTo(3f, 15.075f)
                verticalLineTo(17.5f)
                curveTo(3f, 17.633f, 3.053f, 17.76f, 3.146f, 17.854f)
                curveTo(3.24f, 17.947f, 3.367f, 18f, 3.5f, 18f)
                horizontalLineTo(15f)
                curveTo(18.313f, 18f, 21f, 15.314f, 21f, 12f)
                curveTo(21f, 8.686f, 18.313f, 6f, 15f, 6f)
                close()
                moveTo(3.5f, 5f)
                curveTo(3.102f, 5f, 2.721f, 5.158f, 2.439f, 5.439f)
                curveTo(2.158f, 5.721f, 2f, 6.102f, 2f, 6.5f)
                verticalLineTo(8.925f)
                curveTo(2f, 9.136f, 2.067f, 9.342f, 2.191f, 9.513f)
                lineTo(2.809f, 10.362f)
                curveTo(2.933f, 10.533f, 3f, 10.739f, 3f, 10.95f)
                verticalLineTo(13.05f)
                curveTo(3f, 13.261f, 2.933f, 13.467f, 2.809f, 13.638f)
                lineTo(2.191f, 14.487f)
                curveTo(2.067f, 14.658f, 2f, 14.864f, 2f, 15.075f)
                verticalLineTo(17.5f)
                curveTo(2f, 17.898f, 2.158f, 18.279f, 2.439f, 18.561f)
                curveTo(2.721f, 18.842f, 3.102f, 19f, 3.5f, 19f)
                horizontalLineTo(15f)
                curveTo(18.866f, 19f, 22f, 15.866f, 22f, 12f)
                curveTo(22f, 8.134f, 18.866f, 5f, 15f, 5f)
                horizontalLineTo(3.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF36E881)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15f, 8f)
                horizontalLineTo(6.5f)
                curveTo(6.367f, 8f, 6.24f, 8.053f, 6.146f, 8.146f)
                curveTo(6.053f, 8.24f, 6f, 8.367f, 6f, 8.5f)
                verticalLineTo(15.5f)
                curveTo(6f, 15.633f, 6.053f, 15.76f, 6.146f, 15.854f)
                curveTo(6.24f, 15.947f, 6.367f, 16f, 6.5f, 16f)
                horizontalLineTo(15f)
                curveTo(16.061f, 16f, 17.078f, 15.579f, 17.828f, 14.828f)
                curveTo(18.579f, 14.078f, 19f, 13.061f, 19f, 12f)
                curveTo(19f, 10.939f, 18.579f, 9.922f, 17.828f, 9.172f)
                curveTo(17.078f, 8.421f, 16.061f, 8f, 15f, 8f)
                close()
                moveTo(6.5f, 7f)
                curveTo(6.102f, 7f, 5.721f, 7.158f, 5.439f, 7.439f)
                curveTo(5.158f, 7.721f, 5f, 8.102f, 5f, 8.5f)
                verticalLineTo(15.5f)
                curveTo(5f, 15.898f, 5.158f, 16.279f, 5.439f, 16.561f)
                curveTo(5.721f, 16.842f, 6.102f, 17f, 6.5f, 17f)
                horizontalLineTo(15f)
                curveTo(17.761f, 17f, 20f, 14.762f, 20f, 12f)
                curveTo(20f, 9.238f, 17.761f, 7f, 15f, 7f)
                horizontalLineTo(6.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF36E881))) {
                moveTo(18f, 12f)
                curveTo(18f, 12.398f, 17.842f, 12.779f, 17.561f, 13.061f)
                curveTo(17.279f, 13.342f, 16.898f, 13.5f, 16.5f, 13.5f)
                curveTo(16.102f, 13.5f, 15.721f, 13.342f, 15.439f, 13.061f)
                curveTo(15.158f, 12.779f, 15f, 12.398f, 15f, 12f)
                curveTo(15f, 11.602f, 15.158f, 11.221f, 15.439f, 10.939f)
                curveTo(15.721f, 10.658f, 16.102f, 10.5f, 16.5f, 10.5f)
                curveTo(16.898f, 10.5f, 17.279f, 10.658f, 17.561f, 10.939f)
                curveTo(17.842f, 11.221f, 18f, 11.602f, 18f, 12f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF36E881)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(7f, 10f)
                curveTo(7f, 9.735f, 7.105f, 9.48f, 7.293f, 9.293f)
                curveTo(7.48f, 9.105f, 7.735f, 9f, 8f, 9f)
                horizontalLineTo(13.5f)
                curveTo(13.765f, 9f, 14.02f, 9.105f, 14.207f, 9.293f)
                curveTo(14.395f, 9.48f, 14.5f, 9.735f, 14.5f, 10f)
                verticalLineTo(14f)
                curveTo(14.5f, 14.265f, 14.395f, 14.52f, 14.207f, 14.707f)
                curveTo(14.02f, 14.895f, 13.765f, 15f, 13.5f, 15f)
                horizontalLineTo(8f)
                curveTo(7.735f, 15f, 7.48f, 14.895f, 7.293f, 14.707f)
                curveTo(7.105f, 14.52f, 7f, 14.265f, 7f, 14f)
                verticalLineTo(10f)
                close()
                moveTo(7.5f, 11.5f)
                curveTo(7.5f, 10.97f, 7.711f, 10.461f, 8.086f, 10.086f)
                curveTo(8.461f, 9.711f, 8.97f, 9.5f, 9.5f, 9.5f)
                curveTo(10.03f, 9.5f, 10.539f, 9.711f, 10.914f, 10.086f)
                curveTo(11.289f, 10.461f, 11.5f, 10.97f, 11.5f, 11.5f)
                curveTo(11.5f, 12.03f, 11.289f, 12.539f, 10.914f, 12.914f)
                curveTo(10.539f, 13.289f, 10.03f, 13.5f, 9.5f, 13.5f)
                curveTo(8.97f, 13.5f, 8.461f, 13.289f, 8.086f, 12.914f)
                curveTo(7.711f, 12.539f, 7.5f, 12.03f, 7.5f, 11.5f)
                close()
                moveTo(9.5f, 10.5f)
                curveTo(9.235f, 10.5f, 8.98f, 10.605f, 8.793f, 10.793f)
                curveTo(8.605f, 10.98f, 8.5f, 11.235f, 8.5f, 11.5f)
                curveTo(8.5f, 11.765f, 8.605f, 12.02f, 8.793f, 12.207f)
                curveTo(8.98f, 12.395f, 9.235f, 12.5f, 9.5f, 12.5f)
                curveTo(9.765f, 12.5f, 10.02f, 12.395f, 10.207f, 12.207f)
                curveTo(10.395f, 12.02f, 10.5f, 11.765f, 10.5f, 11.5f)
                curveTo(10.5f, 11.235f, 10.395f, 10.98f, 10.207f, 10.793f)
                curveTo(10.02f, 10.605f, 9.765f, 10.5f, 9.5f, 10.5f)
                close()
                moveTo(13.125f, 12.5f)
                curveTo(13.125f, 12.481f, 13.117f, 12.455f, 13.086f, 12.427f)
                curveTo(13.044f, 12.392f, 12.992f, 12.374f, 12.938f, 12.375f)
                horizontalLineTo(12.563f)
                curveTo(12.455f, 12.375f, 12.395f, 12.435f, 12.38f, 12.472f)
                curveTo(12.362f, 12.518f, 12.335f, 12.559f, 12.301f, 12.595f)
                curveTo(12.267f, 12.63f, 12.226f, 12.658f, 12.181f, 12.678f)
                curveTo(12.135f, 12.697f, 12.087f, 12.708f, 12.037f, 12.708f)
                curveTo(11.988f, 12.709f, 11.939f, 12.7f, 11.894f, 12.682f)
                curveTo(11.848f, 12.664f, 11.806f, 12.637f, 11.771f, 12.602f)
                curveTo(11.735f, 12.568f, 11.707f, 12.527f, 11.688f, 12.482f)
                curveTo(11.668f, 12.437f, 11.658f, 12.388f, 11.657f, 12.339f)
                curveTo(11.656f, 12.29f, 11.665f, 12.241f, 11.684f, 12.195f)
                curveTo(11.823f, 11.843f, 12.181f, 11.625f, 12.563f, 11.625f)
                horizontalLineTo(12.938f)
                curveTo(13.413f, 11.625f, 13.875f, 11.976f, 13.875f, 12.5f)
                curveTo(13.874f, 12.627f, 13.845f, 12.753f, 13.789f, 12.867f)
                curveTo(13.733f, 12.982f, 13.652f, 13.082f, 13.552f, 13.161f)
                lineTo(13.012f, 13.625f)
                horizontalLineTo(13.5f)
                curveTo(13.599f, 13.625f, 13.695f, 13.665f, 13.765f, 13.735f)
                curveTo(13.835f, 13.805f, 13.875f, 13.901f, 13.875f, 14f)
                curveTo(13.875f, 14.099f, 13.835f, 14.195f, 13.765f, 14.265f)
                curveTo(13.695f, 14.335f, 13.599f, 14.375f, 13.5f, 14.375f)
                horizontalLineTo(12f)
                curveTo(11.923f, 14.375f, 11.849f, 14.352f, 11.786f, 14.308f)
                curveTo(11.723f, 14.264f, 11.675f, 14.202f, 11.648f, 14.13f)
                curveTo(11.622f, 14.059f, 11.618f, 13.98f, 11.637f, 13.906f)
                curveTo(11.656f, 13.832f, 11.697f, 13.765f, 11.755f, 13.715f)
                lineTo(13.068f, 12.588f)
                lineTo(13.079f, 12.58f)
                curveTo(13.116f, 12.55f, 13.125f, 12.52f, 13.125f, 12.5f)
                close()
            }
        }.build()

        return _PulseOximeter!!
    }

@Suppress("ObjectPropertyName")
private var _PulseOximeter: ImageVector? = null
