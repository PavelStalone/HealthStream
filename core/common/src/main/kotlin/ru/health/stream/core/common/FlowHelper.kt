package ru.health.stream.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

inline fun <T> Iterable<T>.bindByFlow(isActive: (T) -> Flow<Boolean>): Flow<List<T>> {
    val items = (this as? List<T>) ?: toList()
    if (items.isEmpty()) return flowOf(emptyList())

    val flows = items.map { isActive(it).distinctUntilChanged() }

    return combine(flows) { activeStates ->
        val result = ArrayList<T>(items.size)

        activeStates.forEachIndexed { index, active ->
            if (active) result.add(items[index])
        }
        result
    }
}
