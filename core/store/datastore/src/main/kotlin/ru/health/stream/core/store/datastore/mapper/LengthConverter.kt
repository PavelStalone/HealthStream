package ru.health.stream.core.store.datastore.mapper

import io.github.jacksever.automapper.annotation.AutoConverter
import ru.health.stream.feature.personal.data.model.Length

object LengthConverter {

    @AutoConverter
    fun fromLength(value: Length?): Double? = value?.meters

    @AutoConverter
    fun toLength(value: Double?): Length? = value?.let { Length(meters = value) }
}
