package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.health.stream.source.local.room.entity.resource.ResourceEntity
import ru.health.stream.source.local.room.entity.resource.ResourceWithType

@Dao
internal interface ResourceDao : DeviceDao {

    @Transaction
    @Query("SELECT * FROM resource WHERE id == :id")
    suspend fun getResourceById(id: String): ResourceWithType

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ResourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllResources(entities: List<ResourceEntity>)

    @Transaction
    suspend fun insert(resourceWithType: ResourceWithType) {
        insert(entity = resourceWithType.resourceEntity)
        resourceWithType.deviceEntity?.let { deviceEntity -> insert(entity = deviceEntity) }
    }

    @Transaction
    suspend fun insertAllResourcesWithType(resourcesWithType: List<ResourceWithType>) {
        insertAllResources(entities = resourcesWithType.map { resourceWithType -> resourceWithType.resourceEntity })
        insertAllDevices(entities = resourcesWithType.mapNotNull { resourceWithType -> resourceWithType.deviceEntity })
    }
}
