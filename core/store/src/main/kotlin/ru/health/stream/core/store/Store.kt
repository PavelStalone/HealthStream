package ru.health.stream.core.store

interface Store {

    suspend fun isActive(): Boolean
}
