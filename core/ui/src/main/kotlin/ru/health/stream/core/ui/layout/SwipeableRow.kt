package ru.health.stream.core.ui.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SwipeableRow(
    modifier: Modifier = Modifier,
    swipeWidth: Dp = 40.dp,
    mainContent: @Composable () -> Unit,
    backContent: @Composable RowScope.(progress: Float) -> Unit,
) {
    val actionWidthPx = with(LocalDensity.current) { swipeWidth.toPx() }

    val anchors = remember(actionWidthPx) {
        DraggableAnchors {
            DragState.CLOSED at 0f
            DragState.OPENED at -actionWidthPx
        }
    }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragState.CLOSED,
            anchors = anchors
        )
    }

    val offset = state.offset
    val progress = (-offset / actionWidthPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(swipeWidth * progress)
                .fillMaxHeight(),
            content = {
                backContent(progress)
            }
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(x = offset.roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                )
        ) {
            mainContent()
        }
    }
}

private enum class DragState {

    CLOSED,
    OPENED,
    ;
}
