package ru.health.stream.core.store

import kotlinx.datetime.Instant
import java.util.SortedSet

internal fun <T> List<List<T>>.mergeByTime(instant: (value: T) -> Instant): Set<T> {
    val sortedSet: SortedSet<T> = sortedSetOf(
        comparator = Comparator { value1, value2 -> -(instant(value1).compareTo(instant(value2))) }
    )

    forEach { values -> sortedSet.addAll(values) }
    return sortedSet
}
