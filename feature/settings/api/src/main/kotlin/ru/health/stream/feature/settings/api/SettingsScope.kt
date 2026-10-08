package ru.health.stream.feature.settings.api

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

/**
 * Top-level DSL scope for declaring settings categories and items.
 */
@SettingsDslMarker
interface SettingsScope {

    /**
     * Declares or appends to a category in settings.
     * Categories with identical [key] across different modules are merged automatically.
     *
     * @param key Unique key for the category.
     * @param priority Display priority of the category (higher priority categories appear first).
     * @param header Composable content rendered for the category header in [BoxScope].
     * @param content Scope lambda for declaring items inside this category.
     */
    fun category(
        key: String,
        priority: Int = 0,
        header: @Composable BoxScope.() -> Unit,
        content: CategoryScope.() -> Unit,
    )
}

/**
 * Contributor type alias for registering settings entries in Hilt multibindings.
 */
typealias SettingsEntry = SettingsScope.() -> Unit
