package ru.health.stream.feature.personal.data.repository

import ru.health.stream.feature.personal.data.model.User

interface UserRepository {

    suspend fun getUser(): User?
    suspend fun saveUser(user: User)
}
