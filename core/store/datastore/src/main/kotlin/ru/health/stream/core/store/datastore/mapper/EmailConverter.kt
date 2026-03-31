package ru.health.stream.core.store.datastore.mapper

import io.github.jacksever.automapper.annotation.AutoConverter
import ru.health.stream.feature.personal.data.model.Email

object EmailConverter {

    @AutoConverter
    fun fromEmail(value: Email?): String? = value?.value

    @AutoConverter
    fun toEmail(value: String?): Email? = value?.let { Email(value = value) }
}
