package ru.health.stream.di

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.health.stream.core.starter.ActivityStarter
import ru.health.stream.core.starter.AppStarter

@Module
@InstallIn(SingletonComponent::class)
internal object StarterModule {

    @IntoSet
    @Provides
    fun testAppStarter() = object : AppStarter {
        override fun onCreate() {
            Log.i("App", "onCreate")
        }

        override fun onTerminate() {
            Log.i("App", "onTerminate")
        }
    }

    @IntoSet
    @Provides
    fun testActivityStarter() = object : ActivityStarter {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            Log.i("Activity", "source: $source, event: $event")
        }
    }
}
