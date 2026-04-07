package ru.health.stream.feature.settings.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.feature.settings.navigation.SettingsNavKey

internal fun EntryProviderScope<NavKey>.settingsEntry() {
    entry<SettingsNavKey> {
        SettingsScreen()
    }
}
