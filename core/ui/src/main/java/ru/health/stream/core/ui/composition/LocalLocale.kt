package ru.health.stream.core.ui.composition

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

val LocalLocale = compositionLocalOf { Locale.getDefault() }
