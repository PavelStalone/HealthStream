package ru.health.stream.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.core.ui.model.asText

@Composable
fun TopBar(
    title: UiText,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.align(Alignment.CenterStart),
            content = { navigationIcon() }
        )
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = title.asText(),
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            content = actions
        )
    }
}
