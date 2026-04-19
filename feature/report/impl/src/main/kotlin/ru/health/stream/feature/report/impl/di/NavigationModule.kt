package ru.health.stream.feature.report.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.feature.report.impl.presentation.navigation.reportEntry

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object NavigationModule {

    @IntoSet
    @Provides
    fun provideReportEntry(): EntryProviderScope<NavKey>.(Router<NavKey>) -> Unit = { router ->
        reportEntry(router = router)
    }
}
