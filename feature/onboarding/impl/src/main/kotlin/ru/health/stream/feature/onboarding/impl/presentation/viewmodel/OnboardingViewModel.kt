package ru.health.stream.feature.onboarding.impl.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.health.stream.core.ui.R
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.data.setting.model.AppParam
import ru.health.stream.data.setting.repository.AppParamRepository
import ru.health.stream.feature.onboarding.impl.presentation.model.OnboardingStep
import ru.health.stream.feature.onboarding.impl.presentation.navigation.LocalOnboardingNavKey
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val appParamRepository: AppParamRepository
) : ViewModel() {

    private val onboardingSteps = sequenceOf(
        // Home Screen Steps
        OnboardingStep(
            id = "home",
            screen = LocalOnboardingNavKey.Home,
            text = UiText.Translatable(R.string.onboarding_home_welcome)
        ),
        OnboardingStep(
            id = "home",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "vitals_card",
            text = UiText.Translatable(R.string.onboarding_home_empty)
        ),
        OnboardingStep(
            id = "home",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "ble_button",
            text = UiText.Translatable(R.string.onboarding_home_ble_button)
        ),
        OnboardingStep(
            id = "home_ble_active",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "ble_button",
            text = UiText.Translatable(R.string.onboarding_home_ble_button_active)
        ),
        OnboardingStep(
            id = "home",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "add_measurement_fab",
            text = UiText.Translatable(R.string.onboarding_home_fab_button)
        ),
        OnboardingStep(
            id = "home_fab_active",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "add_measurement_fab",
            text = UiText.Translatable(R.string.onboarding_home_fab_button_active)
        ),
        OnboardingStep(
            id = "home_filled",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "vitals_card",
            text = UiText.Translatable(R.string.onboarding_home_filled_example)
        ),
        OnboardingStep(
            id = "home_filled",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "estimation",
            text = UiText.Translatable(R.string.onboarding_estimation_badge)
        ),
        OnboardingStep(
            id = "home_filled",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "vitals_card",
            text = UiText.Translatable(R.string.onboarding_estimation_delay)
        ),
        OnboardingStep(
            id = "home_filled",
            screen = LocalOnboardingNavKey.Home,
            targetKey = "vitals_card",
            text = UiText.Translatable(R.string.onboarding_week_card)
        ),
        // Measurement Screen Steps
        OnboardingStep(
            id = "measurement",
            screen = LocalOnboardingNavKey.Measurement,
            targetKey = "measurement_chart",
            text = UiText.Translatable(R.string.onboarding_measurement_chart)
        ),
        OnboardingStep(
            id = "measurement",
            screen = LocalOnboardingNavKey.Measurement,
            targetKey = "measurement_data",
            text = UiText.Translatable(R.string.onboarding_measurement_details)
        ),
        OnboardingStep(
            id = "measurement",
            screen = LocalOnboardingNavKey.Measurement,
            targetKey = "measurement_data_title",
            text = UiText.Translatable(R.string.onboarding_expand_group)
        ),
        OnboardingStep(
            id = "measurement_expand",
            screen = LocalOnboardingNavKey.Measurement,
            targetKey = "measurement_card",
            text = UiText.Translatable(R.string.onboarding_measurement_card)
        ),
        OnboardingStep(
            id = "measurement_expand_edit",
            screen = LocalOnboardingNavKey.Measurement,
            targetKey = "measurement_card",
            text = UiText.Translatable(R.string.onboarding_swipe_actions)
        ),
        // Report Screen Steps
        OnboardingStep(
            id = "report",
            screen = LocalOnboardingNavKey.Report,
            text = UiText.Translatable(R.string.onboarding_report_intro)
        ),
        OnboardingStep(
            id = "report_expand",
            screen = LocalOnboardingNavKey.Report,
            targetKey = "report_date_range",
            text = UiText.Translatable(R.string.onboarding_report_date_range)
        ),
        OnboardingStep(
            id = "report_expand",
            screen = LocalOnboardingNavKey.Report,
            targetKey = "report_type_selection",
            text = UiText.Translatable(R.string.onboarding_report_format)
        ),
        OnboardingStep(
            id = "report_expand",
            screen = LocalOnboardingNavKey.Report,
            targetKey = "report_measurement_selection",
            text = UiText.Translatable(R.string.onboarding_report_types)
        ),
        OnboardingStep(
            id = "report_expand",
            screen = LocalOnboardingNavKey.Report,
            targetKey = "report_title_exclude",
            text = UiText.Translatable(R.string.onboarding_report_exclude_block)
        ),
        OnboardingStep(
            id = "report_expand",
            screen = LocalOnboardingNavKey.Report,
            targetKey = "report_card",
            text = UiText.Translatable(R.string.onboarding_report_exclude_item)
        ),
        OnboardingStep(
            id = "report_expand",
            screen = LocalOnboardingNavKey.Report,
            targetKey = "report_generate_button",
            text = UiText.Translatable(R.string.onboarding_report_generate)
        ),
        // Profile Screen Steps
        OnboardingStep(
            id = "profile",
            screen = LocalOnboardingNavKey.Profile,
            text = UiText.Translatable(R.string.onboarding_profile_intro)
        ),
        OnboardingStep(
            id = "profile",
            screen = LocalOnboardingNavKey.Profile,
            text = UiText.Translatable(R.string.onboarding_profile_incomplete)
        ),
        OnboardingStep(
            id = "profile",
            screen = LocalOnboardingNavKey.Profile,
            targetKey = "profile_save_button",
            text = UiText.Translatable(R.string.onboarding_profile_save)
        ),
        OnboardingStep(
            id = "profile",
            screen = LocalOnboardingNavKey.Profile,
            text = UiText.Translatable(R.string.onboarding_finish)
        )
    ).iterator()

    private val _finishEvent = MutableSharedFlow<Unit>()
    val finishEvent = _finishEvent.asSharedFlow()

    val currentStepFlow = MutableStateFlow(onboardingSteps.next())

    fun nextStep() {
        if (onboardingSteps.hasNext()) {
            currentStepFlow.value = onboardingSteps.next()
        } else {
            onFinish()
        }
    }

    private fun onFinish() {
        viewModelScope.launch {
            appParamRepository.setAppParam(AppParam(isFirstStart = false))
            _finishEvent.emit(Unit)
        }
    }
}
