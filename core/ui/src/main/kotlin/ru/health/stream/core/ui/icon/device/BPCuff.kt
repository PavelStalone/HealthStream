package ru.health.stream.core.ui.icon.device

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.icon.Icons

val Icons.Device.BPCuff: ImageVector
    get() {
        if (_BPCuff != null) {
            return _BPCuff!!
        }
        _BPCuff = ImageVector.Builder(
            name = "BPCuff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF36E881))) {
                moveTo(12.5f, 10.75f)
                lineTo(11.75f, 13.25f)
                lineTo(12.5f, 14.25f)
                lineTo(13.25f, 13.25f)
                lineTo(12.5f, 10.75f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF36E881)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(17f, 8.816f)
                curveTo(16.543f, 8.587f, 16.107f, 8.317f, 15.698f, 8.009f)
                curveTo(14.926f, 7.423f, 14f, 6.475f, 14f, 5.225f)
                curveTo(14f, 4.11f, 14.842f, 3f, 16.094f, 3f)
                curveTo(16.675f, 3f, 17.145f, 3.232f, 17.5f, 3.579f)
                curveTo(17.854f, 3.232f, 18.325f, 3f, 18.906f, 3f)
                curveTo(20.158f, 3f, 21f, 4.111f, 21f, 5.224f)
                curveTo(21f, 6.436f, 20.069f, 7.385f, 19.306f, 7.976f)
                curveTo(18.895f, 8.29f, 18.459f, 8.569f, 18f, 8.809f)
                curveTo(18.003f, 9.681f, 18.017f, 10.514f, 18.031f, 11.302f)
                curveTo(18.06f, 13.029f, 18.086f, 14.543f, 17.981f, 15.794f)
                curveTo(17.822f, 17.688f, 17.353f, 19.208f, 15.948f, 20.031f)
                curveTo(14.568f, 20.839f, 12.528f, 21.143f, 10.839f, 20.938f)
                curveTo(9.991f, 20.834f, 9.181f, 20.597f, 8.568f, 20.188f)
                curveTo(8.123f, 19.891f, 7.769f, 19.49f, 7.603f, 18.989f)
                curveTo(6.565f, 18.941f, 5.532f, 18.751f, 4.715f, 18.413f)
                curveTo(3.832f, 18.046f, 3f, 17.413f, 3f, 16.42f)
                verticalLineTo(6.561f)
                horizontalLineTo(3.001f)
                lineTo(3f, 6.5f)
                curveTo(3f, 5.12f, 5.239f, 4f, 8f, 4f)
                curveTo(10.762f, 4f, 13f, 5.12f, 13f, 6.5f)
                curveTo(13f, 6.52f, 13f, 6.541f, 12.998f, 6.561f)
                horizontalLineTo(13f)
                verticalLineTo(9.535f)
                curveTo(13.833f, 9.656f, 14.595f, 10.073f, 15.146f, 10.709f)
                curveTo(15.696f, 11.345f, 16f, 12.159f, 16f, 13f)
                curveTo(15.999f, 13.842f, 15.696f, 14.655f, 15.145f, 15.292f)
                curveTo(14.594f, 15.928f, 13.833f, 16.344f, 13f, 16.465f)
                curveTo(12.976f, 17.472f, 12.171f, 18.11f, 11.283f, 18.472f)
                curveTo(10.547f, 18.771f, 9.642f, 18.935f, 8.717f, 18.985f)
                curveTo(8.817f, 19.118f, 8.953f, 19.243f, 9.125f, 19.357f)
                curveTo(9.559f, 19.648f, 10.2f, 19.853f, 10.96f, 19.945f)
                curveTo(12.485f, 20.131f, 14.293f, 19.841f, 15.443f, 19.167f)
                curveTo(16.399f, 18.608f, 16.831f, 17.54f, 16.985f, 15.71f)
                curveTo(17.085f, 14.509f, 17.06f, 13.08f, 17.032f, 11.392f)
                curveTo(17.015f, 10.533f, 17.005f, 9.675f, 17f, 8.816f)
                close()
                moveTo(16.094f, 4f)
                curveTo(15.503f, 4f, 15f, 4.548f, 15f, 5.224f)
                curveTo(15f, 5.971f, 15.574f, 6.66f, 16.302f, 7.213f)
                curveTo(16.676f, 7.493f, 17.075f, 7.739f, 17.494f, 7.945f)
                curveTo(17.559f, 7.912f, 17.636f, 7.871f, 17.725f, 7.821f)
                curveTo(17.992f, 7.672f, 18.344f, 7.455f, 18.694f, 7.185f)
                curveTo(19.43f, 6.615f, 20f, 5.927f, 20f, 5.225f)
                curveTo(20f, 4.548f, 19.497f, 4f, 18.906f, 4f)
                curveTo(18.494f, 4f, 18.167f, 4.229f, 17.937f, 4.642f)
                lineTo(17.5f, 5.431f)
                lineTo(17.063f, 4.642f)
                curveTo(16.833f, 4.229f, 16.505f, 4f, 16.094f, 4f)
                close()
                moveTo(8f, 9f)
                curveTo(9.635f, 9f, 11.088f, 8.608f, 12f, 8f)
                verticalLineTo(9.535f)
                curveTo(11.167f, 9.655f, 10.404f, 10.071f, 9.853f, 10.707f)
                curveTo(9.302f, 11.344f, 8.998f, 12.157f, 8.998f, 12.999f)
                curveTo(8.998f, 13.841f, 9.301f, 14.655f, 9.852f, 15.291f)
                curveTo(10.403f, 15.928f, 11.165f, 16.344f, 11.998f, 16.465f)
                curveTo(11.976f, 16.854f, 11.662f, 17.237f, 10.906f, 17.545f)
                curveTo(10.143f, 17.855f, 9.09f, 18.011f, 8.005f, 18f)
                curveTo(7.852f, 17.998f, 7.699f, 17.993f, 7.545f, 17.984f)
                verticalLineTo(8.99f)
                curveTo(7.695f, 8.997f, 7.847f, 9f, 8f, 9f)
                close()
                moveTo(12f, 6.5f)
                curveTo(12f, 6.608f, 11.887f, 6.974f, 11.089f, 7.373f)
                curveTo(10.349f, 7.742f, 9.259f, 8f, 8f, 8f)
                curveTo(6.741f, 8f, 5.65f, 7.742f, 4.911f, 7.373f)
                curveTo(4.113f, 6.974f, 4f, 6.608f, 4f, 6.5f)
                curveTo(4f, 6.392f, 4.113f, 6.026f, 4.911f, 5.627f)
                curveTo(5.65f, 5.258f, 6.741f, 5f, 8f, 5f)
                curveTo(9.259f, 5f, 10.35f, 5.258f, 11.089f, 5.627f)
                curveTo(11.887f, 6.026f, 12f, 6.392f, 12f, 6.5f)
                close()
                moveTo(12.5f, 15.5f)
                curveTo(13.163f, 15.5f, 13.799f, 15.237f, 14.268f, 14.768f)
                curveTo(14.737f, 14.299f, 15f, 13.663f, 15f, 13f)
                curveTo(15f, 12.337f, 14.737f, 11.701f, 14.268f, 11.232f)
                curveTo(13.799f, 10.763f, 13.163f, 10.5f, 12.5f, 10.5f)
                curveTo(11.837f, 10.5f, 11.201f, 10.763f, 10.732f, 11.232f)
                curveTo(10.263f, 11.701f, 10f, 12.337f, 10f, 13f)
                curveTo(10f, 13.663f, 10.263f, 14.299f, 10.732f, 14.768f)
                curveTo(11.201f, 15.237f, 11.837f, 15.5f, 12.5f, 15.5f)
                close()
            }
        }.build()

        return _BPCuff!!
    }

@Suppress("ObjectPropertyName")
private var _BPCuff: ImageVector? = null
