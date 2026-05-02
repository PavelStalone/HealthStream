package ru.health.stream

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import ru.health.stream.core.starter.StarterApplication
import javax.inject.Inject

@HiltAndroidApp
class HealthStreamApplication : StarterApplication(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
