package ru.health.stream.feature.personal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.feature.personal.data.repository.UserRepository
import ru.health.stream.feature.personal.infrastructure.UserRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindModule {

        @Binds
        @Singleton
        fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    }
}
