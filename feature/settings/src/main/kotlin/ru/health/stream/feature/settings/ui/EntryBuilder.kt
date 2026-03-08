package ru.health.stream.feature.settings.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.feature.settings.navigation.SettingsScreen

internal fun EntryProviderScope<NavKey>.featureEntryBuilder() {
    entry<SettingsScreen> {
        SettingsScreen()
    }
}
