package ru.health.stream.feature.personal.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.feature.personal.data.navigation.UserInputScreen

internal fun EntryProviderScope<NavKey>.featureEntryBuilder() {
    entry<UserInputScreen> {
        UserInputScreen(modifier = Modifier.fillMaxSize())
    }
}
