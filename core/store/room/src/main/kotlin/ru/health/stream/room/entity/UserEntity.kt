package ru.health.stream.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
internal data class UserEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "height") val height: Double,
    @ColumnInfo(name = "gender") val gender: Boolean,
    @ColumnInfo(name = "birthday") val birthday: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "first_name") val firstName: String,
)
