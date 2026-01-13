package ru.health.stream.feature.settings

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.health.stream.feature.settings.Settings.Category
import ru.health.stream.feature.settings.Settings.Cell
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

abstract class BaseSettings : Settings {

    private val data: SortedMap<Category, SortedSet<Cell>> = TreeMap(
        Comparator.comparing(Category::priority).reversed().thenComparing(Category::key)
    )

    private val _categories: MutableStateFlow<Map<Category, Set<Cell>>> = MutableStateFlow(mapOf())
    override val categories: StateFlow<Map<Category, Set<Cell>>> = _categories

    override fun add(category: Category, cell: Cell): Boolean = synchronized(data) {
        val cellSet = data.getOrPut(category) {
            TreeSet(Comparator.comparing(Cell::priority).reversed().thenComparing(Cell::key))
        }

        cellSet.add(cell)
            .also { isAdded ->
                if (isAdded) _categories.value = data.mapValues { (_, values) -> values.toSet() }
            }
    }

    override fun get(category: Category): Set<Cell> = data.getOrElse(category) { emptySet() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseSettings

        return data == other.data
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }
}

abstract class BaseCategory : Category {

    @OptIn(ExperimentalUuidApi::class)
    override val key: String = Uuid.random().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCategory

        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

@Immutable
abstract class BaseCell : Cell {

    @OptIn(ExperimentalUuidApi::class)
    override val key: String = Uuid.random().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCell

        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
