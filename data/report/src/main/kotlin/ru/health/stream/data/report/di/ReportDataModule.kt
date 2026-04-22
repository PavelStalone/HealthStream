package ru.health.stream.data.report.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.report.repository.ReportRepository
import ru.health.stream.data.report.repository.impl.ReportRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ReportDataModule {

    @Binds
    @Singleton
    fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
}
