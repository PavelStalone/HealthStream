package ru.health.stream.core.ui.composition

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface ScaffoldCustomizer {

    val snackBarHostState: SnackbarHostState

    fun setFab(content: @Composable () -> Unit)
    fun setTopBar(content: @Composable () -> Unit)
    fun setSnackBar(content: @Composable (data: SnackbarData) -> Unit)
}

val LocalScaffoldCustomizer =
    staticCompositionLocalOf<ScaffoldCustomizer> { error("ScaffoldCustomizer not initialized") }
