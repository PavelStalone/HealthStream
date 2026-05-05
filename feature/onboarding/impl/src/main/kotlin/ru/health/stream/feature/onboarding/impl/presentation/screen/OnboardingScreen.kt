package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.health.stream.core.ui.model.asText
import ru.health.stream.feature.onboarding.impl.presentation.component.OnboardingOverlay
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val pagerState = rememberPagerState(pageCount = { 4 })

    val currentStep by viewModel.currentStepFlow.collectAsState()

    LaunchedEffect(currentStep.pageIndex) {
        pagerState.animateScrollToPage(currentStep.pageIndex)
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
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = pagerState,
                userScrollEnabled = false,
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> OnboardingHomeScreen(viewModel)
                    1 -> OnboardingMeasurementScreen(viewModel)
                    2 -> OnboardingReportScreen(viewModel)
                    3 -> OnboardingProfileScreen(viewModel)
                }
            }
        }
    }
}
