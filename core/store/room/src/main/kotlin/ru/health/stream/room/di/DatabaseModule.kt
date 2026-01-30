package ru.health.stream.room.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.room.MeasurementTable
import ru.health.stream.room.VitalDatabase
import ru.health.stream.room.table.HeartRateTable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideVitalDatabase(@ApplicationContext context: Context) =
        VitalDatabase.buildDatabase(context)

    @Provides
    @Singleton
    @Suppress("UNCHECKED_CAST")
    fun provideTables(
        heartRateTable: HeartRateTable,
    ): List<MeasurementTable<HealthMeasurement, *>> = listOf(
        heartRateTable as MeasurementTable<HealthMeasurement, *>,
    )
}
