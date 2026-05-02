package ru.health.stream.source.local.healthconnect.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import jakarta.inject.Singleton
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.ExternalMeasurementSource
import ru.health.stream.source.local.healthconnect.record.BloodPressureSource
import ru.health.stream.source.local.healthconnect.record.BodyWeightSource
import ru.health.stream.source.local.healthconnect.record.HeartRateSource
import ru.health.stream.source.local.healthconnect.record.MeasurementSource
import ru.health.stream.source.local.healthconnect.record.OxygenSaturationSource
import ru.health.stream.source.local.healthconnect.source.HealthConnectMeasurementSource

@Module
@InstallIn(SingletonComponent::class)
internal object HealthConnectModule {

    @Provides
    @Singleton
    @Suppress("UNCHECKED_CAST")
    fun provideMeasurementSources(
        heartRateSource: HeartRateSource,
        bodyWeightSource: BodyWeightSource,
        bloodPressureSource: BloodPressureSource,
        oxygenSaturationSource: OxygenSaturationSource,
    ): List<MeasurementSource<Measurement>> = listOf(
        heartRateSource as MeasurementSource<Measurement>,
        bodyWeightSource as MeasurementSource<Measurement>,
        bloodPressureSource as MeasurementSource<Measurement>,
        oxygenSaturationSource as MeasurementSource<Measurement>,
    )

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @IntoSet
        @Singleton
        fun bindMeasurementSource(impl: HealthConnectMeasurementSource): ExternalMeasurementSource
    }
}
