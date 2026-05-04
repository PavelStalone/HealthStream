package ru.health.stream.core.ui.icon.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons

val Icons.Default.Report: ImageVector
    get() {
        if (_Report != null) {
            return _Report!!
        }
        _Report = ImageVector.Builder(
            name = "Default.Report",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(17f, 4f)
                horizontalLineTo(7f)
                curveTo(5.895f, 4f, 5f, 4.895f, 5f, 6f)
                verticalLineTo(19f)
                curveTo(5f, 20.105f, 5.895f, 21f, 7f, 21f)
                horizontalLineTo(17f)
                curveTo(18.105f, 21f, 19f, 20.105f, 19f, 19f)
                verticalLineTo(6f)
                curveTo(19f, 4.895f, 18.105f, 4f, 17f, 4f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(9f, 9f)
                horizontalLineTo(15f)
                moveTo(9f, 13f)
                horizontalLineTo(15f)
                moveTo(9f, 17f)
                horizontalLineTo(13f)
            }
        }.build()

        return _Report!!
    }

@Suppress("ObjectPropertyName")
private var _Report: ImageVector? = null
