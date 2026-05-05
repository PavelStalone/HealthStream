package ru.health.stream.feature.onboarding.impl.presentation.composition

import androidx.compose.runtime.compositionLocalOf
import ru.health.stream.feature.onboarding.impl.presentation.component.OnboardingScope

internal val LocalOnboardingScope = compositionLocalOf<OnboardingScope> { error("Not initialized") }
