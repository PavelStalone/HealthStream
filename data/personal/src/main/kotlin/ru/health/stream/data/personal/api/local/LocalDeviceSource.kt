package ru.health.stream.data.personal.api.local

import ru.health.stream.data.personal.model.User

interface LocalUserSource {

    suspend fun getUser(): User?
    suspend fun writeUser(user: User)
}
