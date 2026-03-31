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
    modifier: Modifier = Modifier,
) {
    val router = LocalRouter.current

    Column(
        modifier = modifier
    ) {
        TopBar(
            title = UiText.NonTranslatable("Профиль"),
            navigationIcon = {
                IconButton(onClick = { router.pop() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )
        UserInputContent(
            modifier = Modifier.fillMaxSize(),
            onSuccess = { router.pop() },
            onCancel = { router.pop() }
        )
    }
}
