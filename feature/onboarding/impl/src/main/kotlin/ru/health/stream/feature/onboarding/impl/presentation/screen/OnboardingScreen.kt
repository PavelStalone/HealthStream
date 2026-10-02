package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ru.health.stream.core.ui.model.asText
import ru.health.stream.feature.onboarding.impl.presentation.component.OnboardingOverlay
import ru.health.stream.feature.onboarding.impl.presentation.navigation.LocalOnboardingNavKey
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val localBackStack = rememberNavBackStack(LocalOnboardingNavKey.Home)

    val currentStep by viewModel.currentStepFlow.collectAsState()

    LaunchedEffect(currentStep.screen) {
        localBackStack[0] = currentStep.screen
    }

    LaunchedEffect(viewModel) {
        viewModel.finishEvent.collect { onFinish() }
    }

    OnboardingOverlay(
        modifier = Modifier.fillMaxSize(),
        text = currentStep.text.asText(),
        targetKey = currentStep.targetKey,
        onNext = { viewModel.nextStep() },
    ) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
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
