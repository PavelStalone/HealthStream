package ru.health.stream.source.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "heartRate",
    indices = [Index(value = ["created_at"], unique = false)],
)
internal data class HeartRateEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "pulse_rate") val pulse: Int,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "resource") val resource: ResourceEntity,
)
