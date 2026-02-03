package ru.health.stream.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.health.stream.room.entity.DeviceEntity

@Dao
internal interface DeviceDao {

    @Query("SELECT * FROM device WHERE id == :id")
    suspend fun getDeviceById(id: String): DeviceEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeviceEntity)
}
