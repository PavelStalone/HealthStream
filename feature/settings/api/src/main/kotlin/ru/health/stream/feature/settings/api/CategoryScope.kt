package ru.health.stream.feature.settings.api

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

/**
 * Scope for declaring settings items (fields) within a category.
 */
@SettingsDslMarker
interface CategoryScope {

    /**
     * Registers a settings item (field) inside the category.
     *
     * @param key Unique key for the item within the category.
     * @param priority Display priority of the item inside the category (higher priority items appear first).
     * @param content Composable UI content rendered within a [BoxScope].
     */
    fun item(
        key: String,
        priority: Int = 0,
        content: @Composable BoxScope.() -> Unit,
    )
}
