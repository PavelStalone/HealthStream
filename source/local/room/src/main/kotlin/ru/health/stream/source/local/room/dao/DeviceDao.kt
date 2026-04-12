package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.health.stream.source.local.room.entity.resource.DeviceEntity

@Dao
internal interface DeviceDao {

    @Query("SELECT * FROM device WHERE id == :id")
    suspend fun getDeviceById(id: String): DeviceEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDevices(entities: List<DeviceEntity>)
}
