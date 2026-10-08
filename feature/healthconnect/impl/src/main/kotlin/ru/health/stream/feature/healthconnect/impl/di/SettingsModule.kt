package ru.health.stream.feature.healthconnect.impl.di

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.health.stream.feature.healthconnect.impl.presentation.component.HealthConnectSettingsField
import ru.health.stream.feature.settings.api.SettingsEntry
import ru.health.stream.source.local.healthconnect.HealthConnectManager

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object SettingsModule {

    @Provides
    @IntoSet
    fun provideHealthConnectSettings(
        healthConnectManager: HealthConnectManager
    ): SettingsEntry = {
        category(
            key = "data_sync",
            header = { Text("Синхронизация данных") },
            priority = 100,
        ) {
            item(
                key = "health_connect",
                priority = 100,
            ) {
                HealthConnectSettingsField(
                    modifier = Modifier.fillMaxWidth(),
                    healthConnectManager = healthConnectManager,
                )
            }
        }
    }
}
