package ru.health.stream.core.ui.model

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

@Immutable
sealed interface UiIcon {

    val contentDescription: UiText?

    data class Resource(
        @DrawableRes val resId: Int,
        override val contentDescription: UiText? = null,
    ) : UiIcon

    data class Vector(
        val imageVector: ImageVector,
        override val contentDescription: UiText? = null,
    ) : UiIcon
}

@Composable
fun UiIcon.drawIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    when (this) {
        is UiIcon.Resource -> Icon(
            modifier = modifier,
            tint = tint,
            painter = painterResource(id = resId),
            contentDescription = contentDescription?.asText(),
        )

        is UiIcon.Vector -> Icon(
            modifier = modifier,
            tint = tint,
            imageVector = imageVector,
            contentDescription = contentDescription?.asText(),
        )
    }
}
