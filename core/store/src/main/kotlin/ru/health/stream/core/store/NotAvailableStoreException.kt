package ru.health.stream.core.store

class NotAvailableStoreException(message: String? = null) : RuntimeException(message)

internal suspend fun <T : Store> Iterable<T>.checkAvailable(): Iterable<T> = this.also { stores ->
    if (stores.all { store -> !store.isActive() }) throw NotAvailableStoreException("All stores are not active")
}
