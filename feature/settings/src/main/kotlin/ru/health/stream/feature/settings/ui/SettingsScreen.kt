package ru.health.stream.feature.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.health.stream.core.ui.shape.multiShape
import ru.health.stream.core.ui.theme.HealthStreamTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()

    val categories by viewModel.categoriesFlow.collectAsStateWithLifecycle(emptyList())

    LazyColumn(modifier = modifier) {
        categories.forEach { category ->
            category.cells.forEachIndexed { index, cell ->
                item(key = cell.key) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium.multiShape(
                            index = index,
                            count = category.cells.size,
                        ),
                        content = { cell.Content() },
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
@Preview
internal fun SettingsPreview() {
    HealthStreamTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SettingsScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
