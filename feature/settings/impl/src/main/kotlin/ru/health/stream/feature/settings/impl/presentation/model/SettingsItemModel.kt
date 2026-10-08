package ru.health.stream.feature.settings.impl.presentation.model

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsItemModel(
    val key: String,
    val priority: Int,
    val content: @Composable BoxScope.() -> Unit,
)
