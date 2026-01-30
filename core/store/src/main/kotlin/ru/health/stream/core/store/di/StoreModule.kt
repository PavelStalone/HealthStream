package ru.health.stream.core.store.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.core.store.measurement.LocalHealthMeasurementSourceImpl
import ru.health.stream.feature.vitals.source.local.LocalHealthMeasurementSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object StoreModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @Singleton
        fun provideLocalHeartRateStore(impl: LocalHealthMeasurementSourceImpl): LocalHealthMeasurementSource
    }
}
