package ru.health.stream.feature.settings.impl.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.shape.multiShape
import ru.health.stream.core.ui.theme.DeviceThemePreviews
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.feature.settings.impl.domain.SettingsBuilderImpl
import ru.health.stream.feature.settings.impl.presentation.model.SettingsCategoryModel

@Composable
internal fun SettingsContent(
    categories: List<SettingsCategoryModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        categories.forEach { category ->
            item(key = category.key) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    category.header(this)
                }
            }

            itemsIndexed(
                items = category.items,
                key = { _, field -> "${category.key}_${field.key}" },
            ) { index, field ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium.multiShape(
                        index = index,
                        count = category.items.size,
                    ),
                    content = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            field.content(this)
                        }
                    },
                )
            }

            item(key = "spacer_${category.key}") {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
@DeviceThemePreviews
private fun SettingsContentPreview() {
    val sampleCategories = SettingsBuilderImpl().apply {
        category(
            key = "group_1",
            header = { Text("Test Group 1") },
            priority = 100,
        ) {
            repeat(3) { index ->
                item(
                    key = "cell_1_$index",
                    priority = 10 - index,
                ) {
                    Text("Test Cell $index")
                }
            }
        }

        category(
            key = "group_2",
            header = { Text("Test Group 2") },
            priority = 50,
        ) {
            repeat(2) { index ->
                item(
                    key = "cell_2_$index",
                    priority = 10 - index,
                ) {
                    Text("Test Cell $index")
                }
            }
        }
    }.build()

    HealthStreamTheme {
        SettingsContent(
            categories = sampleCategories,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        )
    }
}
