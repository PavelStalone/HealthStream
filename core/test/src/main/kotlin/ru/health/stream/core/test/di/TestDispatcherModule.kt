package ru.health.stream.core.test.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestDispatcherModule {

    @Provides
    @Singleton
    @OptIn(ExperimentalCoroutinesApi::class)
    fun providesTestDispatcher(): TestDispatcher = UnconfinedTestDispatcher()
}
