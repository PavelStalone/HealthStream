package ru.health.stream.feature.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

interface SettingsCell {

    val category: Int

    @Composable
    fun RowScope.Content()
}
