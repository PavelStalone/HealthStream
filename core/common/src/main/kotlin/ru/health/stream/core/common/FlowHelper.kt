package ru.health.stream.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

inline fun <reified T> Iterable<T>.bindByFlow(isActive: (T) -> Flow<Boolean>): Flow<Iterable<T>> =
    combine(
        flows = map { item -> isActive(item).map { isActive -> if (isActive) item else null } }
    ) { items -> items.filterNotNull() }
