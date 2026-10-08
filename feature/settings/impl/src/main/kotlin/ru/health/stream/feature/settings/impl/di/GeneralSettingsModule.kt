package ru.health.stream.feature.settings.impl.di

import androidx.compose.material3.Text
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.feature.settings.api.SettingsEntry

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object GeneralSettingsModule {

    @Provides
    @IntoSet
    fun provideGeneralSettings(): SettingsEntry = {
        category(
            key = "general",
            header = { Text("Основные") },
            priority = 100,
        ) {
            item(
                key = "theme_setting",
                priority = 10,
            ) {
                Text("Тема оформления")
            }
        }
    }
}
