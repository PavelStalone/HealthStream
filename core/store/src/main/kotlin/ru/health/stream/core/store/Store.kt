package ru.health.stream.core.store

import kotlinx.coroutines.flow.Flow

interface Store {

    val isActive: Flow<Boolean>
}
