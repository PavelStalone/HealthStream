package ru.health.stream.feature.onboarding.impl.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import ru.health.stream.feature.onboarding.impl.presentation.composition.LocalOnboardingScope

@Composable
internal fun OnboardingOverlay(
    targetKey: String?,
    text: String,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val targetCoordinates = remember { mutableStateMapOf<String, Rect>() }
    var overlayOffset by remember { mutableStateOf(Offset.Zero) }

    val scope = remember(targetCoordinates) {
        OnboardingScope { key, rect ->
            targetCoordinates[key] = rect
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayOffset = coordinates.positionInWindow()
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        CompositionLocalProvider(LocalOnboardingScope provides scope) {
            content()
        }

        val targetRect = targetKey?.let { targetCoordinates[it] }?.translate(-overlayOffset)
        val animatedRect by animateRectAsState(
            targetValue = targetRect ?: Rect(
                left = screenWidth.value / 2f,
                top = screenHeight.value / 2f,
                right = screenWidth.value / 2f,
                bottom = screenHeight.value / 2f
            ),
            animationSpec = tween(durationMillis = 500),
            label = "SpotlightAnimation"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNext
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = Color.Black.copy(alpha = 0.7f))
                if (targetKey != null) {
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = animatedRect.topLeft,
                        size = animatedRect.size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
            }

            val density = LocalDensity.current
            val tooltipPadding = 16.dp

            val isBottomHalf = if (targetRect != null) {
                val rectCenterY = targetRect.center.y
                with(density) { rectCenterY.toDp() > screenHeight / 2 }
            } else false

            val targetYOffset by animateDpAsState(
                targetValue = if (targetRect != null) {
                    if (isBottomHalf) {
                        with(density) { targetRect.top.toDp() } - 160.dp
                    } else {
                        with(density) { targetRect.bottom.toDp() } + 16.dp
                    }
                } else {
                    screenHeight / 3
                },
                animationSpec = tween(durationMillis = 300),
                label = "TooltipPositionAnimation"
            )

            Card(
                modifier = Modifier
                    .padding(horizontal = tooltipPadding)
                    .offset(y = targetYOffset)
                    .widthIn(max = screenWidth - 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AnimatedContent(
                    targetState = text,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 200))
                            .togetherWith(fadeOut(animationSpec = tween(durationMillis = 200))))
                    },
                    label = "TextAnimation"
                ) { targetText ->
                    Text(
                        modifier = Modifier.padding(all = 16.dp),
                        text = targetText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

internal fun interface OnboardingScope {
    fun onPositioned(key: String, rect: Rect)
}

internal fun Modifier.onboardingTarget(
    key: String,
    scope: OnboardingScope
): Modifier = this.onGloballyPositioned { coordinates ->
    val rect = Rect(
        size = coordinates.size.toSize(),
        offset = coordinates.positionInWindow(),
    )

    scope.onPositioned(key, rect)
}
