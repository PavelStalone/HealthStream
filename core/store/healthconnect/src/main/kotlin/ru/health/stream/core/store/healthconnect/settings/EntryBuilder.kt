package ru.health.stream.core.store.healthconnect.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.core.store.healthconnect.navigation.HealthConnectSettings

internal fun EntryProviderScope<NavKey>.featureEntryBuilder() {
    entry<HealthConnectSettings> {
        SettingsScreen()
    }
}
