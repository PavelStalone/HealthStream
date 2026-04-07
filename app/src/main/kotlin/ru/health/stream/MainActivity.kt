package ru.health.stream

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.arttttt.nav3router.Router
import dagger.hilt.android.AndroidEntryPoint
import ru.health.stream.core.navigation.NavHost
import ru.health.stream.core.starter.StarterActivity
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.fill.Favorite
import ru.health.stream.core.ui.icon.fill.Settings
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.home.api.navigation.HomeNavKey
import ru.health.stream.feature.settings.navigation.SettingsNavKey
import ru.health.stream.feature.user.api.navigation.UserNavKey
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : StarterActivity() {

    @Inject
    lateinit var navigationRouter: Router<NavKey>

    @Inject
    lateinit var entryProviders: Set<@JvmSuppressWildcards EntryProviderScope<NavKey>.(Router<NavKey>) -> Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
//
//        logI("availabilityStatus: $availabilityStatus")
//        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
//            return // early return as there is no viable integration
//        }
//        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
//            // Optionally redirect to package installer to find a provider, for example:
//            val uriString =
//                "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
//            startActivity(
//                Intent(Intent.ACTION_VIEW).apply {
//                    setPackage("com.android.vending")
//                    data = uriString.toUri()
//                    putExtra("overlay", true)
//                    putExtra("callerId", packageName)
//                }
//            )
//            return
//        }

        enableEdgeToEdge()
        setContent {
            HealthStreamTheme(
                darkTheme = false, // TODO: Remove after release - shoplikpavel 2026-03-30
                dynamicColor = false,
            ) {
                val backStack = rememberNavBackStack(
                    elements = arrayOf(
                        HomeNavKey,
                        UserNavKey,
                    )
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AppBottomBar(
                            backStack = backStack,
                            onTabClick = { screen ->
                                navigationRouter.replaceStack(screen)
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        backStack = backStack,
                        router = navigationRouter,
                    ) { backStack, onBack, router ->
                        NavDisplay(
                            modifier = Modifier.padding(paddingValues = innerPadding),
                            onBack = onBack,
                            backStack = backStack,
                            sceneStrategy = DialogSceneStrategy(),
                            entryProvider = entryProvider {
                                entryProviders.forEach { provider -> this.provider(router) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    backStack: List<NavKey>,
    onTabClick: (NavKey) -> Unit
) {
    val tabs = remember {
        listOf(
            BottomTab.Vitals,
            BottomTab.Settings,
        )
    }
    val tabKeys = remember { tabs.map { tab -> tab.screen }.toSet() }
    val activeTabKey by remember(backStack) {
        derivedStateOf {
            backStack.findLast { stack -> stack in tabKeys }
        }
    }

    val visible by remember(backStack) {
        derivedStateOf {
            backStack.last() !is UserNavKey
        }
    }

    AnimatedVisibility(visible = visible) {
        NavigationBar {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = tab.screen == activeTabKey,
                    onClick = { onTabClick(tab.screen) },
                    icon = {
                        Icon(imageVector = tab.icon, contentDescription = tab.title)
                    },
                    label = {
                        Text(text = tab.title)
                    },
                    alwaysShowLabel = false
                )
            }
        }
    }
}

private sealed class BottomTab(
    val title: String,
    val screen: NavKey,
    val icon: ImageVector,
) {

    data object Vitals : BottomTab(
        title = "Измерения",
        screen = HomeNavKey,
        icon = Icons.Fill.Favorite,
    )

    data object Settings : BottomTab(
        title = "Настройки",
        screen = SettingsNavKey,
        icon = Icons.Fill.Settings,
    )
}
