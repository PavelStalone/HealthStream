package ru.health.stream.data.personal.repository

import ru.health.stream.data.personal.model.User

interface UserRepository {

    suspend fun getUser(): User?
    suspend fun saveUser(user: User)
}
