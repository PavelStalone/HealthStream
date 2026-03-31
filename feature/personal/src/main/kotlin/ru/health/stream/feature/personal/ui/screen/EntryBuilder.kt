package ru.health.stream.feature.personal.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.health.stream.core.navigation.LocalRouter
import ru.health.stream.feature.personal.data.navigation.UserInputFlow

internal fun EntryProviderScope<NavKey>.featureEntryBuilder() {
    entry<UserInputFlow> { key ->
        val router = LocalRouter.current

        UserInputScreen(
            modifier = Modifier.fillMaxSize(),
            onSuccess = { router.replaceCurrent(key.destinationKey) },
        )
    }
}
