package ru.health.stream.feature.personal.source.local

import ru.health.stream.feature.personal.data.model.User

interface LocalUserSource {

    suspend fun getUser(): User?
    suspend fun saveUser(user: User)
}
