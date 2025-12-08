package ru.health.stream.feature.vitals.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.feature.vitals.data.repository.MeasurementRepository
import ru.health.stream.feature.vitals.infrastructure.MeasurementRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @Singleton
        fun bindMeasurementRepository(impl: MeasurementRepositoryImpl): MeasurementRepository
    }
}
