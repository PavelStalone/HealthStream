package ru.health.stream.di

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import jakarta.inject.Provider
import ru.health.stream.core.starter.ActivityStarter
import ru.health.stream.core.starter.AppStarter
import ru.health.stream.worker.MeasurementWorker
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
internal object StarterModule {

    @IntoSet
    @Provides
    fun provideWorkerStarter(
        workManager: Provider<WorkManager>,
    ) = object : AppStarter {

        override fun onCreate() {
            val workRequest = PeriodicWorkRequestBuilder<MeasurementWorker>(15, TimeUnit.MINUTES)
                .build()

            workManager.get().enqueueUniquePeriodicWork(
                request = workRequest,
                uniqueWorkName = "MeasurementWorker",
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            )
        }
    }

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
