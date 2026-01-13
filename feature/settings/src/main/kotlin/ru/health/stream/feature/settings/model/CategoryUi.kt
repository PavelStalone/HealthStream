package ru.health.stream.feature.settings.model

import androidx.compose.runtime.Immutable
import ru.health.stream.feature.settings.Settings

@Immutable
internal data class CategoryUi(
    val cells: Set<Settings.Cell>
)
