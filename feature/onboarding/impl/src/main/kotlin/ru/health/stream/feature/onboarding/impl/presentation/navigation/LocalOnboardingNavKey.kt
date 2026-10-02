package ru.health.stream.feature.onboarding.impl.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface LocalOnboardingNavKey : NavKey {

    @Serializable
    data object Home : LocalOnboardingNavKey

    @Serializable
    data object Measurement : LocalOnboardingNavKey

    @Serializable
    data object Report : LocalOnboardingNavKey

    @Serializable
    data object Profile : LocalOnboardingNavKey
}
