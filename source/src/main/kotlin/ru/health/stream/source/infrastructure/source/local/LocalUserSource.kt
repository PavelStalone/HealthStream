package ru.health.stream.source.infrastructure.source.local

import ru.health.stream.data.personal.model.User

interface LocalUserSource {

    suspend fun getUser(): User?
    suspend fun writeUser(user: User)
}
