package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ru.health.stream.core.ui.composition.LocalScaffoldCustomizer
import ru.health.stream.core.ui.composition.ScaffoldCustomizerImpl
import ru.health.stream.core.ui.model.asText
import ru.health.stream.feature.onboarding.impl.presentation.component.OnboardingOverlay
import ru.health.stream.feature.onboarding.impl.presentation.navigation.LocalOnboardingNavKey
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val customizer = remember(snackBarHostState) {
        ScaffoldCustomizerImpl(snackBarHostState)
    }
    val localBackStack = rememberNavBackStack(LocalOnboardingNavKey.Home)
    val viewModel: OnboardingViewModel = hiltViewModel()

    val currentStep by viewModel.currentStepFlow.collectAsState()

    LaunchedEffect(currentStep.screen) {
        localBackStack[0] = currentStep.screen
    }

    LaunchedEffect(viewModel) {
        viewModel.finishEvent.collect { onFinish() }
    }

    OnboardingOverlay(
        modifier = modifier,
        text = currentStep.text.asText(),
        targetKey = currentStep.targetKey,
        onNext = { viewModel.nextStep() },
    ) {
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
                bottomBar = {}
            ) { innerPadding ->
                NavDisplay(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    backStack = localBackStack,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }).togetherWith(
                            slideOutHorizontally(targetOffsetX = { -it })
                        )
                    },
                    entryProvider = entryProvider {
                        entry<LocalOnboardingNavKey.Home> {
                            OnboardingHomeScreen(viewModel)
                        }
                        entry<LocalOnboardingNavKey.Measurement> {
                            OnboardingMeasurementScreen(viewModel)
                        }
                        entry<LocalOnboardingNavKey.Report> {
                            OnboardingReportScreen(viewModel)
                        }
                        entry<LocalOnboardingNavKey.Profile> {
                            OnboardingProfileScreen(viewModel)
                        }
                    },
                )
            }
        }
    }
}
