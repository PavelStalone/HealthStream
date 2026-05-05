package ru.health.stream.source.local.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.source.infrastructure.source.local.LocalMeasurementSource
import ru.health.stream.source.local.SyncableMeasurementLocalSource

@Module
@InstallIn(SingletonComponent::class)
internal interface LocalSourceModule {

    @Binds
    fun bindLocalMeasurementSource(impl: SyncableMeasurementLocalSource): LocalMeasurementSource
}
