package ru.health.stream.core.ui.icon.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons

val Icons.Default.Bluetooth: ImageVector
    get() {
        if (_Bluetooth != null) {
            return _Bluetooth!!
        }
        _Bluetooth = ImageVector.Builder(
            name = "Default.Bluetooth",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(440f, 880f)
                verticalLineToRelative(-304f)
                lineTo(256f, 760f)
                lineToRelative(-56f, -56f)
                lineToRelative(224f, -224f)
                lineToRelative(-224f, -224f)
                lineToRelative(56f, -56f)
                lineToRelative(184f, 184f)
                verticalLineToRelative(-304f)
                horizontalLineToRelative(40f)
                lineToRelative(228f, 228f)
                lineToRelative(-172f, 172f)
                lineToRelative(172f, 172f)
                lineTo(480f, 880f)
                horizontalLineToRelative(-40f)
                close()
                moveTo(520f, 384f)
                lineTo(596f, 308f)
                lineTo(520f, 234f)
                verticalLineToRelative(150f)
                close()
                moveTo(520f, 726f)
                lineTo(596f, 652f)
                lineTo(520f, 576f)
                verticalLineToRelative(150f)
                close()
            }
        }.build()

        return _Bluetooth!!
    }

@Suppress("ObjectPropertyName")
private var _Bluetooth: ImageVector? = null
