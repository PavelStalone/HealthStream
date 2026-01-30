package ru.health.stream.room.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.health.stream.core.store.measurement.HealthMeasurementSource
import ru.health.stream.room.store.RoomHealthMeasurementSource
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
        fun bindHeartRateStore(impl: RoomHealthMeasurementSource): HealthMeasurementSource
    }
}
