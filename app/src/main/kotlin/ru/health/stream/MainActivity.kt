package ru.health.stream

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.arttttt.nav3router.Router
import dagger.hilt.android.AndroidEntryPoint
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.navigation.NavHost
import ru.health.stream.core.starter.StarterActivity
import ru.health.stream.core.ui.composition.LocalScaffoldCustomizer
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.AccountCircle
import ru.health.stream.core.ui.icon.default.Report
import ru.health.stream.core.ui.icon.fill.Favorite
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.data.setting.repository.AppParamRepository
import ru.health.stream.feature.home.api.navigation.HomeNavKey
import ru.health.stream.feature.onboarding.api.OnboardingNavKey
import ru.health.stream.feature.report.api.navigation.ReportNavKey
import ru.health.stream.feature.user.api.navigation.UserNavKey
import ru.health.stream.permission.AndroidPermissionManager
import ru.health.stream.permission.AndroidPermissionManagerProxy
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : StarterActivity() {

    @Inject
    lateinit var androidPermissionManagerProxy: AndroidPermissionManagerProxy

    @Inject
    lateinit var appParamRepository: AppParamRepository

    @Inject
    lateinit var navigationRouter: Router<NavKey>

    @Inject
    lateinit var entryProviders: Set<@JvmSuppressWildcards EntryProviderScope<NavKey>.(Router<NavKey>) -> Unit>

    private lateinit var androidPermissionManager: AndroidPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logD("Set permissionManager")
        androidPermissionManager = AndroidPermissionManager(this)
        androidPermissionManagerProxy.setManager(androidPermissionManager)

        enableEdgeToEdge()
        setContent {
            HealthStreamTheme {
                val appParam by appParamRepository.appParamFlow.collectAsStateWithLifecycle(
                    initialValue = null
                )

                if (appParam != null) {
                    val startDestination = remember {
                        if (appParam?.isFirstStart == true) OnboardingNavKey else HomeNavKey
                    }

                    val backStack = rememberNavBackStack(startDestination)
                    val snackBarHostState = remember { SnackbarHostState() }
                    val customizer = remember(snackBarHostState) {
                        ScaffoldCustomizerImpl(snackBarHostState)
                    }

                    val isBottomBarVisible by remember(backStack) {
                        derivedStateOf {
                            backStack.none { it == OnboardingNavKey }
                        }
                    }

                    LaunchedEffect(backStack.last()) {
                        customizer.clearAll()
                    }

                    CompositionLocalProvider(LocalScaffoldCustomizer provides customizer) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = { customizer.topBarContent() },
                            floatingActionButton = {
                                AnimatedContent(
                                    targetState = customizer.fabContent,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(100)).togetherWith(
                                            fadeOut(animationSpec = tween(90))
                                        )
                                    }
                                ) { it() }
                            },
                            snackbarHost = {
                                SnackbarHost(customizer.snackBarHostState) { data ->
                                    customizer.snackBarContent(data)
                                }
                            },
                            bottomBar = {
                                AnimatedVisibility(visible = isBottomBarVisible) {
                                    AppBottomBar(
                                        backStack = backStack,
                                        onTabClick = { screen ->
                                            navigationRouter.popTo(HomeNavKey)
                                            navigationRouter.push(screen)
                                        }
                                    )
                                }
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

    override fun onDestroy() {
        logD("Remove permissionManager")
        androidPermissionManagerProxy.removeManager(androidPermissionManager)

        super.onDestroy()
    }
}

@Composable
private fun AppBottomBar(
    backStack: List<NavKey>,
    onTabClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
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
