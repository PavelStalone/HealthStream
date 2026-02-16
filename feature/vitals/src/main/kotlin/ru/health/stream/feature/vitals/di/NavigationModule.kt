package ru.health.stream.feature.vitals.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.feature.vitals.ui.screen.featureEntryBuilder

@Module
@InstallIn(ActivityRetainedComponent::class)
object NavigationModule {

    @IntoSet
    @Provides
    fun provideEntryBuilder(): EntryProviderScope<NavKey>.() -> Unit = { featureEntryBuilder() }
}
