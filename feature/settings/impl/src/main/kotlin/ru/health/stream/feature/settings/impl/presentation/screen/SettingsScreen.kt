package ru.health.stream.feature.settings.impl.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.composition.LocalScaffoldCustomizer
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.settings.impl.presentation.viewmodel.SettingsViewModel

@Composable
internal fun SettingsScreen(
    onBackClick: () -> Unit,
) {
    val scaffoldCustomizer = LocalScaffoldCustomizer.current
    val viewModel: SettingsViewModel = hiltViewModel()
    val categories = viewModel.categoriesState

    LaunchedEffect(Unit) {
        scaffoldCustomizer.setTopBar {
            TopBar(
                title = UiText.NonTranslatable(value = "Настройки"),
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            contentDescription = null,
                            imageVector = Icons.Default.ArrowBack,
                        )
                    }
                },
            )
        }
    }

    SettingsContent(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        categories = categories,
    )
}
