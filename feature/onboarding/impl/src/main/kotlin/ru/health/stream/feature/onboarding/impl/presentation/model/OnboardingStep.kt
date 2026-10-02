package ru.health.stream.feature.onboarding.impl.presentation.model

import androidx.compose.runtime.Immutable
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.onboarding.impl.presentation.navigation.LocalOnboardingNavKey

@Immutable
internal data class OnboardingStep(
    val id: String,
    val text: UiText,
    val screen: LocalOnboardingNavKey,
    val targetKey: String? = null,
)
