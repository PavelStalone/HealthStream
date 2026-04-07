package ru.health.stream.source.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

@Entity(tableName = "device")
internal data class DeviceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "mac_address") val macAddress: String,
    @ColumnInfo(name = "last_measured") val lastMeasured: Instant,
    @ColumnInfo(name = "status") val status: ResourceEntity.DeviceEntity.Status,
)
