package ru.health.stream.feature.settings.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.health.stream.feature.settings.SettingsCell
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsCells: Set<@JvmSuppressWildcards SettingsCell>,
) : ViewModel() {


}
