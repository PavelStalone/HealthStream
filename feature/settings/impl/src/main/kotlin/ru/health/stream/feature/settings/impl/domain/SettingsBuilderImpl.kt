package ru.health.stream.feature.settings.impl.domain

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import ru.health.stream.feature.settings.api.CategoryScope
import ru.health.stream.feature.settings.api.SettingsScope
import ru.health.stream.feature.settings.impl.presentation.model.SettingsCategoryModel
import ru.health.stream.feature.settings.impl.presentation.model.SettingsItemModel

internal class SettingsBuilderImpl : SettingsScope {

    private val categoriesMap = mutableMapOf<String, CategoryBuilderImpl>()

    override fun category(
        key: String,
        priority: Int,
        header: @Composable BoxScope.() -> Unit,
        content: CategoryScope.() -> Unit,
    ) {
        val categoryBuilder = categoriesMap.getOrPut(key) {
            CategoryBuilderImpl(
                key = key,
                priority = priority,
                header = header,
            )
        }
        if (priority > categoryBuilder.priority) {
            categoryBuilder.priority = priority
            categoryBuilder.header = header
        }

        categoryBuilder.content()
    }

    fun build(): List<SettingsCategoryModel> = categoriesMap.values
        .sortedByDescending { it.priority }
        .map { it.buildModel() }
}

private class CategoryBuilderImpl(
    val key: String,
    var priority: Int,
    var header: @Composable BoxScope.() -> Unit,
) : CategoryScope {

    private val itemsMap = mutableMapOf<String, SettingsItemModel>()

    override fun item(
        key: String,
        priority: Int,
        content: @Composable BoxScope.() -> Unit,
    ) {
        itemsMap[key] = SettingsItemModel(
            key = key,
            priority = priority,
            content = content,
        )
    }

    fun buildModel(): SettingsCategoryModel = SettingsCategoryModel(
        key = key,
        header = header,
        priority = priority,
        items = itemsMap.values.sortedByDescending { it.priority },
    )
}
