package ru.health.stream.source.local.room.converter

import androidx.room.TypeConverter
import io.github.jacksever.automapper.annotation.AutoConverter
import ru.health.stream.data.personal.model.Email

internal object EmailConverter {

    @AutoConverter
    @TypeConverter
    fun fromEmail(value: Email?): String? = value?.value

    @AutoConverter
    @TypeConverter
    fun toEmail(value: String?): Email? = value?.let { Email(value = value) }
}
