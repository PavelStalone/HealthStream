package ru.health.stream.feature.vitals.ui.screen

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.feature.vitals.data.navigation.MainVitalsScreen

internal fun EntryProviderScope<NavKey>.featureEntryBuilder() {
    entry<MainVitalsScreen> {
        MainVitalsScreen()
    }
}
