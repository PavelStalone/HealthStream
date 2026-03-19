package ru.health.stream.core.ui.icon.device

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.DeviceIcons

val DeviceIcons.Glucose: ImageVector
    get() {
        if (_Glucose != null) {
            return _Glucose!!
        }
        _Glucose = ImageVector.Builder(
            name = "Glucose",
            defaultWidth = 22.dp,
            defaultHeight = 21.dp,
            viewportWidth = 22f,
            viewportHeight = 21f
        ).apply {
            path(fill = SolidColor(Color(0xFF36E881))) {
                moveTo(12.475f, 20.775f)
                curveTo(12.008f, 20.775f, 11.571f, 20.675f, 11.163f, 20.475f)
                curveTo(10.755f, 20.275f, 10.409f, 19.992f, 10.125f, 19.625f)
                lineTo(4.675f, 12.7f)
                lineTo(5.15f, 12.2f)
                curveTo(5.483f, 11.85f, 5.883f, 11.642f, 6.35f, 11.575f)
                curveTo(6.817f, 11.508f, 7.25f, 11.6f, 7.65f, 11.85f)
                lineTo(9.5f, 12.975f)
                verticalLineTo(2.775f)
                curveTo(9.5f, 2.492f, 9.596f, 2.254f, 9.788f, 2.063f)
                curveTo(9.98f, 1.872f, 10.217f, 1.776f, 10.5f, 1.775f)
                curveTo(10.783f, 1.774f, 11.024f, 1.87f, 11.225f, 2.063f)
                curveTo(11.426f, 2.256f, 11.526f, 2.493f, 11.525f, 2.775f)
                verticalLineTo(16.575f)
                lineTo(9.1f, 15.075f)
                lineTo(11.7f, 18.4f)
                curveTo(11.8f, 18.533f, 11.917f, 18.629f, 12.05f, 18.688f)
                curveTo(12.183f, 18.747f, 12.325f, 18.776f, 12.475f, 18.775f)
                horizontalLineTo(18f)
                curveTo(18.55f, 18.775f, 19.021f, 18.579f, 19.413f, 18.188f)
                curveTo(19.805f, 17.797f, 20.001f, 17.326f, 20f, 16.775f)
                verticalLineTo(9.775f)
                curveTo(20f, 9.492f, 20.096f, 9.254f, 20.288f, 9.063f)
                curveTo(20.48f, 8.872f, 20.717f, 8.776f, 21f, 8.775f)
                curveTo(21.283f, 8.774f, 21.52f, 8.87f, 21.713f, 9.063f)
                curveTo(21.906f, 9.256f, 22.001f, 9.493f, 22f, 9.775f)
                verticalLineTo(16.775f)
                curveTo(22f, 17.875f, 21.608f, 18.817f, 20.825f, 19.6f)
                curveTo(20.042f, 20.383f, 19.1f, 20.775f, 18f, 20.775f)
                horizontalLineTo(12.475f)
                close()
                moveTo(13f, 11.775f)
                verticalLineTo(6.775f)
                curveTo(13f, 6.492f, 13.096f, 6.254f, 13.288f, 6.063f)
                curveTo(13.48f, 5.872f, 13.717f, 5.776f, 14f, 5.775f)
                curveTo(14.283f, 5.774f, 14.52f, 5.87f, 14.713f, 6.063f)
                curveTo(14.906f, 6.256f, 15.001f, 6.493f, 15f, 6.775f)
                verticalLineTo(11.775f)
                horizontalLineTo(13f)
                close()
                moveTo(16.5f, 11.775f)
                verticalLineTo(7.775f)
                curveTo(16.5f, 7.492f, 16.596f, 7.254f, 16.788f, 7.063f)
                curveTo(16.98f, 6.872f, 17.217f, 6.776f, 17.5f, 6.775f)
                curveTo(17.783f, 6.774f, 18.02f, 6.87f, 18.213f, 7.063f)
                curveTo(18.406f, 7.256f, 18.501f, 7.493f, 18.5f, 7.775f)
                verticalLineTo(11.775f)
                horizontalLineTo(16.5f)
                close()
                moveTo(1.025f, 7.775f)
                curveTo(0.342f, 7.108f, 0f, 6.292f, 0f, 5.325f)
                curveTo(0f, 4.625f, 0.208f, 3.996f, 0.625f, 3.437f)
                curveTo(1.042f, 2.878f, 1.475f, 2.333f, 1.925f, 1.8f)
                lineTo(3.5f, 0f)
                lineTo(5.075f, 1.825f)
                curveTo(5.525f, 2.358f, 5.958f, 2.9f, 6.375f, 3.45f)
                curveTo(6.792f, 4f, 7f, 4.625f, 7f, 5.325f)
                curveTo(7f, 6.292f, 6.658f, 7.108f, 5.975f, 7.775f)
                curveTo(5.292f, 8.442f, 4.467f, 8.775f, 3.5f, 8.775f)
                curveTo(2.533f, 8.775f, 1.708f, 8.442f, 1.025f, 7.775f)
                close()
                moveTo(4.563f, 6.35f)
                curveTo(4.854f, 6.067f, 5f, 5.725f, 5f, 5.325f)
                curveTo(5f, 4.875f, 4.846f, 4.488f, 4.538f, 4.163f)
                curveTo(4.23f, 3.838f, 3.925f, 3.509f, 3.624f, 3.175f)
                lineTo(3.5f, 3.05f)
                lineTo(3.375f, 3.175f)
                curveTo(3.075f, 3.508f, 2.771f, 3.838f, 2.462f, 4.163f)
                curveTo(2.153f, 4.488f, 1.999f, 4.876f, 2f, 5.325f)
                curveTo(2f, 5.725f, 2.146f, 6.067f, 2.438f, 6.35f)
                curveTo(2.73f, 6.633f, 3.084f, 6.775f, 3.5f, 6.775f)
                curveTo(3.916f, 6.775f, 4.27f, 6.633f, 4.563f, 6.35f)
                close()
            }
        }.build()

        return _Glucose!!
    }

@Suppress("ObjectPropertyName")
private var _Glucose: ImageVector? = null
