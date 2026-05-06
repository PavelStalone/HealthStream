package ru.health.stream.source.local.room.source

import ru.health.stream.data.personal.model.User
import ru.health.stream.source.infrastructure.source.local.LocalUserSource
import ru.health.stream.source.local.room.dao.UserDao
import ru.health.stream.source.local.room.mapper.asUser
import ru.health.stream.source.local.room.mapper.asUserEntity
import javax.inject.Inject

internal class RoomUserSource @Inject constructor(
    private val userDao: UserDao,
) : LocalUserSource {

    override suspend fun getUser(): User? = userDao.getUser()?.asUser()
    override suspend fun writeUser(user: User) = userDao.insert(user.asUserEntity())
}
