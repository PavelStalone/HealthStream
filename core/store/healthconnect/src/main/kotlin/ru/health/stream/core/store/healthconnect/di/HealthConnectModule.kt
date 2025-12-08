package ru.health.stream.core.store.healthconnect.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.health.stream.core.store.healthconnect.store.HealthConnectHeartRateStore
import ru.health.stream.core.store.measurement.HeartRateStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object HealthConnectModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @IntoSet
        @Singleton
        fun bindHeartRateStore(impl: HealthConnectHeartRateStore): HeartRateStore
    }
}
