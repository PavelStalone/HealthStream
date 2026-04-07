package ru.health.stream.feature.user.impl.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Immutable
internal data class UserUiState(
    val email: String = "",
    val error: String? = null,
    val heightCm: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val gender: Boolean = true, // true for male, false for female
    val birthday: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
)
