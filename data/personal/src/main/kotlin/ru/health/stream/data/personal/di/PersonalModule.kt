package ru.health.stream.data.personal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.data.personal.repository.impl.UserRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface PersonalModule {

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
