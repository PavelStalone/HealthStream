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
        require(firstName.isNotBlank()) { "Имя не может быть пустым" }
        require(lastName.isNotBlank()) { "Фамилия не может быть пустой" }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        require(birthday <= today) { "День рождения не может быть в будущем" }
    }

    val fullName: String get() = "$lastName $firstName"

    fun datePeriodAfterBirthday(localDate: LocalDate): DatePeriod = localDate - birthday
}
