package ru.health.stream.core.store.healthconnect.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HealthConnectModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

//        @Binds
//        @IntoSet
//        @Singleton
//        fun bindHeartRateStore(impl: LocalHealthConnectHealthMeasurementSource): HealthMeasurementSource
    }
}
