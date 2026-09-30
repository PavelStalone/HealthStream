package ru.health.stream.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.health.stream.core.common.permission.PermissionManager
import ru.health.stream.permission.AndroidPermissionManagerProxy

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindsModule {

        @Binds
        fun bindPermissionManager(impl: AndroidPermissionManagerProxy): PermissionManager
    }
}
