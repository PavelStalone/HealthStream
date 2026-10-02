package ru.health.stream.feature.onboarding.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.feature.onboarding.impl.presentation.navigation.onboardingEntry

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object NavigationModule {

    @IntoSet
    @Provides
    fun provideOnboardingEntry(): EntryProviderScope<NavKey>.(Router<NavKey>) -> Unit = { router ->
        onboardingEntry(router = router)
    }
}
