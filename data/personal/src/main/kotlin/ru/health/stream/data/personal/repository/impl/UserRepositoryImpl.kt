package ru.health.stream.data.personal.repository.impl

import jakarta.inject.Inject
import ru.health.stream.data.personal.api.local.LocalUserSource
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.personal.repository.UserRepository

class UserRepositoryImpl @Inject constructor(
    private val localUserSource: LocalUserSource
) : UserRepository {

    override suspend fun getUser(): User? = localUserSource.getUser()
    override suspend fun saveUser(user: User) = localUserSource.writeUser(user)
}
