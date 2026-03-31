package ru.health.stream.feature.personal.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Immutable
data class UserUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val heightCm: String = "",
    val gender: Boolean = true, // true for male, false for female
    val birthday: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val error: String? = null,
)
