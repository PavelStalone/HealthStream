package ru.health.stream.source.infrastructure.repository

import jakarta.inject.Inject
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.source.infrastructure.source.local.LocalUserSource

internal class UserRepositoryImpl @Inject constructor(
    private val localUserSource: LocalUserSource
) : UserRepository {

    override suspend fun getUser(): User? = localUserSource.getUser()
    override suspend fun saveUser(user: User) = localUserSource.writeUser(user)
}
