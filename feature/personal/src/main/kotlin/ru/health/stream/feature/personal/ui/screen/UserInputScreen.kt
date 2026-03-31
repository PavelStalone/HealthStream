package ru.health.stream.feature.personal.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.health.stream.core.navigation.LocalRouter
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.model.UiText

@Composable
fun UserInputScreen(
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        TopBar(
            title = UiText.NonTranslatable("Профиль"),
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )
        UserInputContent(
            modifier = Modifier.fillMaxSize(),
            onCancel = onCancel,
            onSuccess = onSuccess,
        )
    }
}
