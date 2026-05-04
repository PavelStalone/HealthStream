package ru.health.stream.core.ui.icon.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons

val Icons.Default.Blood: ImageVector
    get() {
        if (_Blood != null) {
            return _Blood!!
        }
        _Blood = ImageVector.Builder(
            name = "Default.Blood",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 4f)
                lineTo(12.759f, 3.349f)
                curveTo(12.665f, 3.239f, 12.549f, 3.152f, 12.418f, 3.091f)
                curveTo(12.287f, 3.031f, 12.144f, 3f, 12f, 3f)
                curveTo(11.856f, 3f, 11.713f, 3.031f, 11.582f, 3.091f)
                curveTo(11.451f, 3.152f, 11.335f, 3.239f, 11.241f, 3.349f)
                lineTo(12f, 4f)
                close()
                moveTo(12f, 4f)
                lineTo(11.24f, 3.35f)
                verticalLineTo(3.351f)
                lineTo(11.235f, 3.355f)
                lineTo(11.222f, 3.371f)
                lineTo(11.007f, 3.632f)
                curveTo(10.142f, 4.716f, 9.352f, 5.858f, 8.64f, 7.048f)
                curveTo(7.367f, 9.19f, 6f, 12.133f, 6f, 15f)
                horizontalLineTo(8f)
                curveTo(8f, 12.69f, 9.133f, 10.134f, 10.36f, 8.07f)
                curveTo(11.023f, 6.962f, 11.759f, 5.899f, 12.563f, 4.889f)
                lineTo(12.759f, 4.651f)
                lineTo(12f, 4f)
                close()
                moveTo(18f, 15f)
                curveTo(18f, 12.133f, 16.633f, 9.19f, 15.36f, 7.048f)
                curveTo(14.648f, 5.858f, 13.858f, 4.716f, 12.993f, 3.632f)
                lineTo(12.778f, 3.371f)
                lineTo(12.765f, 3.355f)
                lineTo(12.761f, 3.351f)
                lineTo(12.759f, 3.349f)
                lineTo(12f, 4f)
                lineTo(11.241f, 4.651f)
                lineTo(11.243f, 4.653f)
                lineTo(11.288f, 4.707f)
                curveTo(11.322f, 4.747f, 11.372f, 4.808f, 11.437f, 4.889f)
                curveTo(12.241f, 5.899f, 12.977f, 6.962f, 13.64f, 8.07f)
                curveTo(14.866f, 10.134f, 16f, 12.69f, 16f, 15f)
                horizontalLineTo(18f)
                close()
                moveTo(16f, 15f)
                curveTo(16f, 16.061f, 15.579f, 17.078f, 14.828f, 17.828f)
                curveTo(14.078f, 18.579f, 13.061f, 19f, 12f, 19f)
                verticalLineTo(21f)
                curveTo(13.591f, 21f, 15.117f, 20.368f, 16.243f, 19.243f)
                curveTo(17.368f, 18.117f, 18f, 16.591f, 18f, 15f)
                horizontalLineTo(16f)
                close()
                moveTo(12f, 19f)
                curveTo(10.939f, 19f, 9.922f, 18.579f, 9.172f, 17.828f)
                curveTo(8.421f, 17.078f, 8f, 16.061f, 8f, 15f)
                horizontalLineTo(6f)
                curveTo(6f, 16.591f, 6.632f, 18.117f, 7.757f, 19.243f)
                curveTo(8.883f, 20.368f, 10.409f, 21f, 12f, 21f)
                verticalLineTo(19f)
                close()
            }
        }.build()

        return _Blood!!
    }

@Suppress("ObjectPropertyName")
private var _Blood: ImageVector? = null
