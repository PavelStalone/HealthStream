package ru.health.stream.core.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface UiText {

    data class NonTranslatable(val value: String) : UiText

    data class Translatable(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText
}

@Composable
fun UiText.asText(): String = when (this) {
    is UiText.NonTranslatable -> value
    is UiText.Translatable -> stringResource(id = resId, formatArgs = args.toTypedArray())
}
