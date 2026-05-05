package ru.health.stream

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ru.health.stream.core.ui.icon.default.AccountCircle
import ru.health.stream.core.ui.icon.default.Report
import ru.health.stream.core.ui.icon.fill.Favorite
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.feature.home.api.navigation.HomeNavKey
import ru.health.stream.feature.onboarding.impl.presentation.screen.OnboardingScreen
import ru.health.stream.feature.report.api.navigation.ReportNavKey
import ru.health.stream.feature.user.api.navigation.UserNavKey
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : StarterActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var navigationRouter: Router<NavKey>

    @Inject
    lateinit var entryProviders: Set<@JvmSuppressWildcards EntryProviderScope<NavKey>.(Router<NavKey>) -> Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    ),
                )

                var onBoarding by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    onBoarding = userRepository.getUser() == null // TODO: Use DataStore for this flag - shoplikpavel 2026-05-05
                }

                if (onBoarding) {
                    OnboardingScreen(
                        onFinish = { onBoarding = false }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            AppBottomBar(
                                backStack = backStack,
                                onTabClick = { screen ->
                                    navigationRouter.popTo(HomeNavKey)
                                    navigationRouter.push(screen)
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
                                    entryProviders.forEach { provider -> provider(router) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    backStack: List<NavKey>,
    modifier: Modifier = Modifier,
    onTabClick: (NavKey) -> Unit,
) {
    val tabs = remember {
        listOf(
            BottomTab.Vitals,
            BottomTab.Report,
            BottomTab.Profile,
        )
    }
    val tabKeys = remember { tabs.map { tab -> tab.screen }.toSet() }
    val activeTabKey by remember(backStack) {
        derivedStateOf {
            backStack.findLast { stack -> stack in tabKeys }
        }
    }

    NavigationBar(modifier = modifier) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tab.screen == activeTabKey,
                onClick = { onTabClick(tab.screen) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                    )
                },
                label = {
                    Text(text = tab.title)
                },
            )
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

    data object Report : BottomTab(
        title = "Отчет",
        screen = ReportNavKey,
        icon = Icons.Default.Report,
    )

    data object Profile : BottomTab(
        title = "Профиль",
        screen = UserNavKey,
        icon = Icons.Default.AccountCircle,
    )
}
