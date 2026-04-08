package ru.health.stream.feature.settings

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Settings {

    val categories: StateFlow<Map<Category, Set<Cell>>>

    fun add(category: Category, cell: Cell): Boolean
    operator fun get(category: Category): Set<Cell>

    interface Category {

        val key: String
        val priority: Int
    }

    interface Cell {

        val key: String
        val priority: Int
        val isEnabled: Flow<Boolean>

        @Composable
        fun Content()
    }
}

object GeneralSettings : BaseSettings()
