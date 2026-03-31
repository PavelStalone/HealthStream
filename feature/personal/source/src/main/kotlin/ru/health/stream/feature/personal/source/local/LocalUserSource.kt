package ru.health.stream.feature.personal.source.local

import ru.health.stream.feature.personal.data.model.User

interface LocalUserSource {

    fun getUser(): User?
    fun saveUser(user: User)
}
