package ru.health.stream.feature.settings.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import ru.health.stream.feature.settings.api.navigation.SettingsNavKey
import ru.health.stream.feature.settings.impl.presentation.screen.SettingsScreen

internal fun EntryProviderScope<NavKey>.settingsEntry(router: Router<NavKey>) {
    entry<SettingsNavKey> {
        SettingsScreen(
            onBackClick = { router.pop() }
        )
    }
}
