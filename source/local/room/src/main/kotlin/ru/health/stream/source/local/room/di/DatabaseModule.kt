package ru.health.stream.source.local.room.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.local.room.MeasurementTable
import ru.health.stream.source.local.room.SqlCipherKeyManager
import ru.health.stream.source.local.room.VitalDatabase
import ru.health.stream.source.local.room.table.BloodPressureTable
import ru.health.stream.source.local.room.table.BodyWeightTable
import ru.health.stream.source.local.room.table.HeartRateTable
import ru.health.stream.source.local.room.table.OxygenSaturationTable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideVitalDatabase(
        @ApplicationContext context: Context,
        sqlCipherKeyManager: SqlCipherKeyManager,
    ) = VitalDatabase.buildDatabase(
        context = context,
        sqlCipherKeyManager = sqlCipherKeyManager,
    )

    @Provides
    @Singleton
    @Suppress("UNCHECKED_CAST")
    fun provideTables(
        heartRateTable: HeartRateTable,
        bodyWeightTable: BodyWeightTable,
        bloodPressureTable: BloodPressureTable,
        oxygenSaturationTable: OxygenSaturationTable,
    ): List<MeasurementTable<Measurement>> = listOf(
        heartRateTable as MeasurementTable<Measurement>,
        bodyWeightTable as MeasurementTable<Measurement>,
        bloodPressureTable as MeasurementTable<Measurement>,
        oxygenSaturationTable as MeasurementTable<Measurement>,
    )
}
