package ru.health.stream.core.store.datastore.mapper

import io.github.jacksever.automapper.annotation.AutoConverter
import kotlinx.datetime.LocalDate

object LocalDateConverter {

    @AutoConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @AutoConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(value) }
}
