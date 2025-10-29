package ru.health.stream.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.health.stream.core.starter.AppStarter

@Module
@InstallIn(SingletonComponent::class)
object StarterModule {

    @IntoSet
    @Provides
    fun test(): AppStarter = object : AppStarter {
        override fun onCreate() {
            Log.i("App", "onCreate")
        }

        override fun onTerminate() {
            Log.i("App", "onTerminate")
        }
    }
}
