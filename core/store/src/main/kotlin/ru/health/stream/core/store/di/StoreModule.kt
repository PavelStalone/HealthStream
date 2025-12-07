package ru.health.stream.core.store.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.core.store.heartrate.HeartRateStore
import ru.health.stream.core.store.heartrate.LocalHeartRateStoreImpl
import ru.health.stream.feature.vitals.source.local.LocalHeartRateStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object StoreModule {

    @Provides
    @Singleton
    fun provideLocalHeartRateStore(
        sources: Set<HeartRateStore>
    ): LocalHeartRateStore = LocalHeartRateStoreImpl(sources = sources)
}
