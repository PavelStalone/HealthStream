package ru.health.stream.room.source

import ru.health.stream.feature.personal.data.model.User
import ru.health.stream.feature.personal.source.local.LocalUserSource
import ru.health.stream.room.dao.UserDao
import ru.health.stream.room.mapper.asUser
import ru.health.stream.room.mapper.asUserEntity
import javax.inject.Inject

internal class RoomUserSource @Inject constructor(
    private val userDao: UserDao,
) : LocalUserSource {

    override suspend fun getUser(): User? = userDao.getUser()?.asUser()
    override suspend fun saveUser(user: User) = userDao.insert(user.asUserEntity())
}
