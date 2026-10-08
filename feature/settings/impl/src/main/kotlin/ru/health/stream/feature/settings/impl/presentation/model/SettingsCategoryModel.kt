package ru.health.stream.feature.settings.impl.presentation.model

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsCategoryModel(
    val key: String,
    val priority: Int,
    val items: List<SettingsItemModel>,
    val header: @Composable BoxScope.() -> Unit,
)
