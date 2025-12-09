package ru.health.stream.room.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.room.VitalDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {

    @Provides
    @Singleton
    fun provideHeartRateDao(db: VitalDatabase) = db.heartRateDao()
}
