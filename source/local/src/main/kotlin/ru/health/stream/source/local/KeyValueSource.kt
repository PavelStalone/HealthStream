package ru.health.stream.source.local

import kotlinx.coroutines.flow.Flow

interface KeyValueSource {

    suspend fun <T> getValue(key: String): T?
    suspend fun <T> saveValue(key: String, value: T)

    fun <T> observe(key: String): Flow<T>
}
