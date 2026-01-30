package ru.health.stream.core.communication.ble.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.core.communication.ble.source.RemoteDeviceSourceImpl
import ru.health.stream.feature.vitals.source.remote.RemoteDeviceSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SourceModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @Singleton
        fun provideRemoteDeviceSource(impl: RemoteDeviceSourceImpl): RemoteDeviceSource
    }
}