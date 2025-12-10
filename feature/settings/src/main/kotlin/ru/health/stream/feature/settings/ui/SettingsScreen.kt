package ru.health.stream.feature.settings.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()

    LazyColumn(
        modifier = modifier
    ) {

    }
}
