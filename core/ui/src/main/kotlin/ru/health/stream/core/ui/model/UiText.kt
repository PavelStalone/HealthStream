package ru.health.stream.core.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface UiText {

    data class NonTranslatable(val value: String) : UiText

    data class Translatable(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    data class App(val packageName: String) : UiText
}

@Composable
fun UiText.asText(): String = when (this) {
    is UiText.NonTranslatable -> value
    is UiText.Translatable -> stringResource(id = resId, formatArgs = args.toTypedArray())
    is UiText.App -> {
        val context = LocalContext.current

        runCatching {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)

            pm.getApplicationLabel(info).toString()
        }.getOrElse { packageName.substringAfterLast('.') }
    }
}
