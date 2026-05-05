package ru.health.stream.feature.onboarding.impl.presentation.model

import androidx.compose.runtime.Immutable
import ru.health.stream.core.ui.model.UiText

@Immutable
internal data class OnboardingStep(
    val id: String,
    val text: UiText,
    val pageIndex: Int,
    val targetKey: String? = null,
)
