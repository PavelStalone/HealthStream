package ru.health.stream.source.local.room.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.health.stream.data.personal.api.local.LocalUserSource
import ru.health.stream.data.vitals.api.local.LocalDeviceSource
import ru.health.stream.source.local.PrimaryMeasurementSource
import ru.health.stream.source.local.room.source.RoomDeviceSource
import ru.health.stream.source.local.room.source.RoomMeasurementSource
import ru.health.stream.source.local.room.source.RoomUserSource
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
        fun bindPrimaryMeasurementSource(impl: RoomMeasurementSource): PrimaryMeasurementSource

        @Binds
        @IntoSet
        @Singleton
        fun bindDeviceSource(impl: RoomDeviceSource): LocalDeviceSource

        @Binds
        @Singleton
        fun bindUserSource(impl: RoomUserSource): LocalUserSource
    }
}
