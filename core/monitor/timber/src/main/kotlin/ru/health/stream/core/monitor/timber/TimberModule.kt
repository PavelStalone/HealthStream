package ru.health.stream.core.monitor.timber

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.core.starter.AppStarter
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object TimberModule {

    @Provides
    fun provideTimberTreeStarter() = object : AppStarter {

        override fun onCreate() {
            Timber.plant(TimberTree())
        }
    }
}
