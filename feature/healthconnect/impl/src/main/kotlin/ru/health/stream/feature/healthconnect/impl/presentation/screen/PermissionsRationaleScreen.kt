package ru.health.stream.feature.healthconnect.impl.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.core.ui.theme.ThemePreviews

@Composable
fun PermissionsRationaleScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { innerPadding ->
        PermissionsRationaleContent(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
            ),
        )
    }
}

@Composable
@ThemePreviews
private fun PermissionsRationaleScreenPreview() {
    HealthStreamTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PermissionsRationaleScreen()
        }
    }
}
