package ru.health.stream.source.local.room.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.VitalDatabase
import ru.health.stream.source.local.room.table.BloodPressureTable
import ru.health.stream.source.local.room.table.HeartRateTable
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
        bloodPressureTable: BloodPressureTable,
    ): List<MeasurementTable<Measurement>> = listOf(
        heartRateTable as MeasurementTable<Measurement>,
        bloodPressureTable as MeasurementTable<Measurement>,
    )
}
