package ru.health.stream.source.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.health.stream.data.setting.repository.AppParamRepository
import ru.health.stream.source.datastore.infrastructure.AppParamRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    private const val PREFERENCES_NAME = "params"

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(PREFERENCES_NAME) }
    )

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @Singleton
        fun bindAppParamRepository(impl: AppParamRepositoryImpl): AppParamRepository
    }
}
