package ru.health.stream.di

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.core.ui.R
import ru.health.stream.feature.settings.api.SettingsEntry
import ru.health.stream.presentation.component.LanguageSettingsField

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object SettingsModule {

    @Provides
    @IntoSet
    fun provideLanguageSettings(): SettingsEntry = {
        category(
            key = "general",
            header = { Text(stringResource(R.string.settings_general_header)) },
            priority = 200,
        ) {
            item(
                key = "language",
                priority = 100,
            ) {
                LanguageSettingsField(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
