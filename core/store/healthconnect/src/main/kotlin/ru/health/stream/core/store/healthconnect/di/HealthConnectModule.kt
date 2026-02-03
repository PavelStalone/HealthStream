package ru.health.stream.core.store.healthconnect.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import jakarta.inject.Singleton
import ru.health.stream.core.store.healthconnect.record.HeartRateSource
import ru.health.stream.core.store.healthconnect.record.MeasurementSource
import ru.health.stream.core.store.healthconnect.store.HealthConnectMeasurementSource
import ru.health.stream.core.store.vitals.HealthMeasurementSource
import ru.health.stream.feature.vitals.data.model.HealthMeasurement

@Module
@InstallIn(SingletonComponent::class)
internal object HealthConnectModule {

    @Provides
    @Singleton
    @Suppress("UNCHECKED_CAST")
    fun provideMeasurementSources(
        heartRateSource: HeartRateSource,
    ): List<MeasurementSource<HealthMeasurement>> = listOf(
        heartRateSource as MeasurementSource<HealthMeasurement>,
    )

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @IntoSet
        @Singleton
        fun bindHealthMeasurementSource(impl: HealthConnectMeasurementSource): HealthMeasurementSource
    }
}
