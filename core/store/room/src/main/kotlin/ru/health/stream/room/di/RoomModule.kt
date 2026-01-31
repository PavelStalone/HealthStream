package ru.health.stream.room.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.health.stream.core.store.vitals.DeviceSource
import ru.health.stream.core.store.vitals.HealthMeasurementSource
import ru.health.stream.room.source.RoomDeviceSource
import ru.health.stream.room.source.RoomHealthMeasurementSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @IntoSet
        @Singleton
        fun bindHealthMeasurementSource(impl: RoomHealthMeasurementSource): HealthMeasurementSource

        @Binds
        @IntoSet
        @Singleton
        fun bindDeviceSource(impl: RoomDeviceSource): DeviceSource
    }
}
