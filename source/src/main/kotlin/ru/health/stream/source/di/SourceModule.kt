package ru.health.stream.source.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.report.repository.ReportRepository
import ru.health.stream.data.vitals.repository.MeasurementRepository
import ru.health.stream.source.infrastructure.repository.MeasurementRepositoryImpl
import ru.health.stream.source.infrastructure.repository.ReportRepositoryImpl
import ru.health.stream.source.infrastructure.repository.UserRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface SourceModule {

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    fun bindMeasurementRepository(impl: MeasurementRepositoryImpl): MeasurementRepository
}
