package ru.health.stream.room.converter

import androidx.room.TypeConverter
import io.github.jacksever.automapper.annotation.AutoConverter
import ru.health.stream.feature.personal.data.model.Length

internal object LengthConverter {

    @AutoConverter
    @TypeConverter
    fun fromLength(value: Length?): Double? = value?.meters

    @AutoConverter
    @TypeConverter
    fun toLength(value: Double?): Length? = value?.let { Length(meters = value) }
}
