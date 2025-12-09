package ru.health.stream.room.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

internal object InstantConverter {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(value) }
    }
}
