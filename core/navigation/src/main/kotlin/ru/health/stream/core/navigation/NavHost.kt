package ru.health.stream.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Nav3Host
import com.arttttt.nav3router.Router
import com.arttttt.nav3router.rememberRouter

@Composable
fun NavHost(
    backStack: NavBackStack<NavKey>,
    router: Router<NavKey> = rememberRouter(),
    content: @Composable (
        backStack: NavBackStack<NavKey>,
        onBack: () -> Unit,
        router: Router<NavKey>,
    ) -> Unit,
) {
    Nav3Host(
        backStack = backStack,
        router = router,
    ) { backStack, onBack, router ->
        CompositionLocalProvider(LocalRouter provides router) {
            content(backStack, onBack, router)
        }
    }
}

val LocalRouter = compositionLocalOf<Router<NavKey>> { error("Router not provided") }
