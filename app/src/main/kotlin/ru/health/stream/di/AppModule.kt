package ru.health.stream.di

import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ru.health.stream.feature.personal.data.navigation.UserInputFlow
import ru.health.stream.feature.settings.navigation.SettingsScreen
import ru.health.stream.feature.vitals.data.navigation.MainVitalsScreen

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideSerializersModule() = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(UserInputFlow::class)
            subclass(SettingsScreen::class)
            subclass(MainVitalsScreen::class)
        }
    }
}
