package ru.health.stream.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.health.stream.room.entity.DeviceEntity
import ru.health.stream.room.entity.UserEntity

@Dao
internal interface UserDao {

    @Query("SELECT * FROM user WHERE id == 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserEntity)
}
