package ru.health.stream.feature.personal.infrastructure

import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logV
import ru.health.stream.feature.personal.data.model.User
import ru.health.stream.feature.personal.data.repository.UserRepository
import ru.health.stream.feature.personal.source.local.LocalUserSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UserRepositoryImpl @Inject constructor(
    val localUserSrc: LocalUserSource,
) : UserRepository {

    override suspend fun getUser(): User? {
        logV("getUser called")

        val result = localUserSrc.getUser()
        logD("getUser result: $result")

        return result
    }

    override suspend fun saveUser(user: User) {
        logV("saveUser called: $user")

        localUserSrc.saveUser(user)
    }
}
