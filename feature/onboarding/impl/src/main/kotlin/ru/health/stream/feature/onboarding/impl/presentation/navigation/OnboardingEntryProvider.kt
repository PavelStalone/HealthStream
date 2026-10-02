package ru.health.stream.feature.onboarding.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import ru.health.stream.feature.onboarding.api.OnboardingNavKey
import ru.health.stream.feature.onboarding.impl.presentation.screen.OnboardingScreen
import ru.health.stream.feature.user.api.navigation.UserNavKey

internal fun EntryProviderScope<NavKey>.onboardingEntry(router: Router<NavKey>) {
    entry<OnboardingNavKey> {
        OnboardingScreen(
            onFinish = { router.replaceCurrent(UserNavKey) }
        )
    }
}
