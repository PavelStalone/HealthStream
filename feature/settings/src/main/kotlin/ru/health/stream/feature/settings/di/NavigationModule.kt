package ru.health.stream.feature.settings.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.feature.settings.ui.settingsEntry

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object NavigationModule {

    @IntoSet
    @Provides
    fun provideSettingsEntry(): EntryProviderScope<NavKey>.(Router<NavKey>) -> Unit = { settingsEntry() }
}
