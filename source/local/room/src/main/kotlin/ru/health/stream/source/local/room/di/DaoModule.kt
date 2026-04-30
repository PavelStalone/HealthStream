package ru.health.stream.source.local.room.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.source.local.room.VitalDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {

    @Provides
    @Singleton
    fun provideUserDao(db: VitalDatabase) = db.userDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: VitalDatabase) = db.noteDao()

    @Provides
    @Singleton
    fun provideDeviceDao(db: VitalDatabase) = db.deviceDao()

    @Provides
    @Singleton
    fun provideResourceDao(db: VitalDatabase) = db.resourceDao()

    @Provides
    @Singleton
    fun provideHeartRateDao(db: VitalDatabase) = db.heartRateDao()

    @Provides
    @Singleton
    fun provideBloodPressureDao(db: VitalDatabase) = db.bloodPressureDao()
}
