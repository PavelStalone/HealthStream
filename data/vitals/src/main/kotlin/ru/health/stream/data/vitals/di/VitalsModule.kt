package ru.health.stream.data.vitals.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.vitals.repository.MeasurementRepository
import ru.health.stream.data.vitals.repository.impl.MeasurementRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface VitalsModule {

    @Binds
    fun bindMeasurementRepository(impl: MeasurementRepositoryImpl): MeasurementRepository
}
