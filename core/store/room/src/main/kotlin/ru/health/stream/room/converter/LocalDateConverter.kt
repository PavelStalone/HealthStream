package ru.health.stream.room.converter

import androidx.room.TypeConverter
import io.github.jacksever.automapper.annotation.AutoConverter
import kotlinx.datetime.LocalDate

internal object LocalDateConverter {

    @AutoConverter
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @AutoConverter
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(value) }
}
