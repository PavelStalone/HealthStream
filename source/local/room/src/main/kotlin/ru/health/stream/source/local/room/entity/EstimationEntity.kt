package ru.health.stream.source.local.room.entity

import androidx.room.ColumnInfo

internal data class EstimationEntity(
    @ColumnInfo(name = "level") val level: Level,
    @ColumnInfo(name = "description") val description: String? = null,
) {

    enum class Level {

        LOW,
        NORMAL,
        HIGH,
        EXTRA_HIGH,
        ;
    }
}
