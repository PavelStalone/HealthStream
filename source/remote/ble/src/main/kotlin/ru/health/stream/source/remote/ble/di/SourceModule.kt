package ru.health.stream.source.remote.ble.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.source.infrastructure.source.remote.RemoteMeasurementSource
import ru.health.stream.source.remote.ble.source.BleMeasurementSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SourceModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @Singleton
        fun bindMeasurementSource(impl: BleMeasurementSource): RemoteMeasurementSource
    }
}
