package ru.health.stream.source.local.file.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.source.infrastructure.source.local.ReportFileGenerator
import ru.health.stream.source.local.file.ReportFileGeneratorImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface PdfModule {

    @Binds
    fun bindReportFileGenerator(impl: ReportFileGeneratorImpl): ReportFileGenerator
}
