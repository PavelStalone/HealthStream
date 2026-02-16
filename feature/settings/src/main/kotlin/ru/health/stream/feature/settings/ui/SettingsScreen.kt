package ru.health.stream.feature.settings.ui

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.health.stream.core.ui.shape.multiShape

@Composable
internal fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()

    val categories by viewModel.categoriesFlow.collectAsStateWithLifecycle(emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
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
