package ru.health.stream.source.local

interface KeyValueSource {

    suspend fun <T> getValue(key: String): T?
    suspend fun <T> saveValue(key: String, value: T)
}
