package ru.health.stream.core.store

import ru.health.stream.feature.settings.Settings.Category

object StoreCategory : Category {

    override val key: String = "StoreCategoryKey"
    override val priority: Int = 0
}
