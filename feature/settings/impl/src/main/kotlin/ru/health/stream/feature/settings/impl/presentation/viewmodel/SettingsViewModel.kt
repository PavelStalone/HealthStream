package ru.health.stream.feature.settings.impl.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import ru.health.stream.feature.settings.api.SettingsEntry
import ru.health.stream.feature.settings.impl.domain.SettingsBuilderImpl
import ru.health.stream.feature.settings.impl.presentation.model.SettingsCategoryModel

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsEntries: Set<@JvmSuppressWildcards SettingsEntry>,
) : ViewModel() {

    val categoriesState by lazy { buildCategories() }

    private fun buildCategories(): List<SettingsCategoryModel> = SettingsBuilderImpl().apply {
        settingsEntries.forEach { entry -> entry(this) }
    }.build()
}
