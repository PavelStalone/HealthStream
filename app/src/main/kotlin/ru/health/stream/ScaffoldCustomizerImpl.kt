package ru.health.stream

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.health.stream.core.ui.composition.ScaffoldCustomizer

internal class ScaffoldCustomizerImpl(
    override val snackBarHostState: SnackbarHostState
) : ScaffoldCustomizer {

    var fabContent by mutableStateOf<(@Composable () -> Unit)>({})
        private set

    var topBarContent by mutableStateOf<(@Composable () -> Unit)>({})
        private set

    var snackBarContent by mutableStateOf<(@Composable (data: SnackbarData) -> Unit)>(
        DEFAULT_SNACK_BAR
    )
        private set

    override fun setFab(content: @Composable (() -> Unit)) {
        fabContent = content
    }

    override fun setTopBar(content: @Composable (() -> Unit)) {
        topBarContent = content
    }

    override fun setSnackBar(content: @Composable ((data: SnackbarData) -> Unit)) {
        snackBarContent = content
    }

    fun clearAll() {
        setFab {  }
        setSnackBar(content = DEFAULT_SNACK_BAR)
    }

    companion object {

        val DEFAULT_SNACK_BAR: @Composable (data: SnackbarData) -> Unit = { Snackbar(it) }
    }
}
