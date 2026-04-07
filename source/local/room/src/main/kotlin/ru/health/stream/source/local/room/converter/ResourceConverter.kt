package ru.health.stream.source.local.room.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import ru.health.stream.source.local.room.entity.ResourceEntity

internal object ResourceConverter {

    @TypeConverter
    fun fromResource(value: ResourceEntity?): String? {
        return value?.let { Json.encodeToString(ResourceEntity.serializer(), value) }
    }

    @TypeConverter
    fun toResource(value: String?): ResourceEntity? {
        return value?.let { Json.decodeFromString(value) }
    }
}
