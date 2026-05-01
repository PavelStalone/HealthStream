package ru.health.stream.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.health.stream.core.ui.component.EstimationLevel
import ru.health.stream.core.ui.theme.ExtraHighLevelContainerColor
import ru.health.stream.core.ui.theme.ExtraHighLevelContentColor
import ru.health.stream.core.ui.theme.HighLevelContainerColor
import ru.health.stream.core.ui.theme.HighLevelContentColor
import ru.health.stream.core.ui.theme.LowLevelContainerColor
import ru.health.stream.core.ui.theme.LowLevelContentColor
import ru.health.stream.core.ui.theme.NormalLevelContainerColor
import ru.health.stream.core.ui.theme.NormalLevelContentColor

enum class UiLevel(
    val text: UiText,
    val contentColor: Color,
    val containerColor: Color,
) {

    LOW(
        text = UiText.NonTranslatable("Низкий"),
        contentColor = LowLevelContentColor,
        containerColor = LowLevelContainerColor,
    ),
    NORMAL(
        text = UiText.NonTranslatable("Нормальный"),
        contentColor = NormalLevelContentColor,
        containerColor = NormalLevelContainerColor,
    ),
    HIGH(
        text = UiText.NonTranslatable("Высокий"),
        contentColor = HighLevelContentColor,
        containerColor = HighLevelContainerColor,
    ),
    CRITICAL(
        text = UiText.NonTranslatable("Критический"),
        contentColor = ExtraHighLevelContentColor,
        containerColor = ExtraHighLevelContainerColor,
    ),
    ;
}

@Composable
fun UiLevel.content(
    modifier: Modifier = Modifier
) {
    EstimationLevel(
        modifier = modifier,
        text = text.asText(),
        contentColor = contentColor,
        containerColor = containerColor,
    )
}
