package ru.health.stream.room.converter

import androidx.room.TypeConverter
import io.github.jacksever.automapper.annotation.AutoConverter
import ru.health.stream.feature.personal.data.model.Email

internal object EmailConverter {

    @AutoConverter
    @TypeConverter
    fun fromEmail(value: Email?): String? = value?.value

    @AutoConverter
    @TypeConverter
    fun toEmail(value: String?): Email? = value?.let { Email(value = value) }
}
