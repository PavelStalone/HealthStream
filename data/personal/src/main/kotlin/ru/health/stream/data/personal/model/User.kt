package ru.health.stream.data.personal.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

data class User(
    val email: Email,
    val height: Length,
    val gender: Boolean, // true for male, false for female
    val lastName: String,
    val firstName: String,
    val birthday: LocalDate,
) {

    init {
        if (firstName.isBlank()) throw UserValidationException(UserValidationException.Reason.FIRST_NAME_EMPTY)
        if (lastName.isBlank()) throw UserValidationException(UserValidationException.Reason.LAST_NAME_EMPTY)

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (birthday > today) throw UserValidationException(UserValidationException.Reason.BIRTHDAY_FUTURE)
    }

    val fullName: String get() = "$lastName $firstName"

    fun datePeriodAfterBirthday(localDate: LocalDate): DatePeriod = localDate - birthday
}

/**
 * Typed validation failures for [User]. Presentation layers should map
 * [reason] to localized messages.
 */
class UserValidationException(val reason: Reason) : Exception() {

    enum class Reason {
        FIRST_NAME_EMPTY,
        LAST_NAME_EMPTY,
        BIRTHDAY_FUTURE,
    }
}