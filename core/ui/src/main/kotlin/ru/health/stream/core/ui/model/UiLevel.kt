package ru.health.stream.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.health.stream.core.ui.R
import ru.health.stream.core.ui.component.EstimationLevel
import ru.health.stream.core.ui.theme.ExtraHighLevelContainerColor
import ru.health.stream.core.ui.theme.ExtraHighLevelContentColor
import ru.health.stream.core.ui.theme.HighLevelContainerColor
import ru.health.stream.core.ui.theme.HighLevelContentColor
import ru.health.stream.core.ui.theme.LowLevelContainerColor
import ru.health.stream.core.ui.theme.LowLevelContentColor
import ru.health.stream.core.ui.theme.NormalLevelContainerColor
import ru.health.stream.core.ui.theme.NormalLevelContentColor
import ru.health.stream.data.vitals.model.Estimation

enum class UiLevel(
    val text: UiText,
    val contentColor: Color,
    val containerColor: Color,
) {

    LOW(
        text = UiText.Translatable(R.string.level_low),
        contentColor = LowLevelContentColor,
        containerColor = LowLevelContainerColor,
    ),
    NORMAL(
        text = UiText.Translatable(R.string.level_normal),
        contentColor = NormalLevelContentColor,
        containerColor = NormalLevelContainerColor,
    ),
    HIGH(
        text = UiText.Translatable(R.string.level_high),
        contentColor = HighLevelContentColor,
        containerColor = HighLevelContainerColor,
    ),
    CRITICAL(
        text = UiText.Translatable(R.string.level_critical),
        contentColor = ExtraHighLevelContentColor,
        containerColor = ExtraHighLevelContainerColor,
    ),
    ;
}

fun Estimation.asUi(): UiLevel = when (level) {
    Estimation.Level.LOW -> UiLevel.LOW
    Estimation.Level.NORMAL -> UiLevel.NORMAL
    Estimation.Level.HIGH -> UiLevel.HIGH
    Estimation.Level.CRITICAL -> UiLevel.CRITICAL
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
