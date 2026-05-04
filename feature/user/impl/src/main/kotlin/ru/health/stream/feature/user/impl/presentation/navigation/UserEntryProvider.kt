package ru.health.stream.feature.user.impl.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import ru.health.stream.feature.user.api.navigation.UserNavKey
import ru.health.stream.feature.user.impl.presentation.screen.UserInputScreen

internal fun EntryProviderScope<NavKey>.userEntry(router: Router<NavKey>) {
    entry<UserNavKey> {
        UserInputScreen(
            modifier = Modifier.fillMaxSize(),
            onSuccess = { router.pop() },
            onBackClick = { router.pop() },
        )
    }
}
