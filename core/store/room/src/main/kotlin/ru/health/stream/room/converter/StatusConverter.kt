package ru.health.stream.room.converter

import androidx.room.TypeConverter
import ru.health.stream.room.entity.ResourceEntity

internal object StatusConverter {

    @TypeConverter
    fun fromStatus(value: ResourceEntity.DeviceEntity.Status?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toStatus(value: String?): ResourceEntity.DeviceEntity.Status? {
        return value?.let { ResourceEntity.DeviceEntity.Status.valueOf(value) }
    }
}
