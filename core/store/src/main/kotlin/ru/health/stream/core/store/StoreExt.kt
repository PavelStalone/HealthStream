package ru.health.stream.core.store

import ru.health.stream.feature.vitals.data.model.SimpleHealthMeasurement
import java.util.SortedSet

internal fun <T : SimpleHealthMeasurement> List<List<T>>.mergeByTimeAndId(): Set<T> {
    val mapById: MutableMap<String, T> = HashMap()
    forEach { values -> mapById.putAll(values.associateBy { value -> value.id }) }

    val sortedSet: SortedSet<T> = sortedSetOf(
        comparator = Comparator { value1, value2 -> -(value1.createdAt.compareTo(value2.createdAt)) }
    )

    sortedSet.addAll(mapById.values)
    return sortedSet
}
