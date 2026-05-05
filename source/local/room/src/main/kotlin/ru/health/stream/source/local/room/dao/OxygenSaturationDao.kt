package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.source.local.room.entity.OxygenSaturationEntity
import ru.health.stream.source.local.room.entity.OxygenSaturationWithMetadata

@Dao
internal interface OxygenSaturationDao : ResourceDao, NoteDao {

    @Query("SELECT * FROM oxygenSaturation WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getByRange(start: Instant, end: Instant): List<OxygenSaturationWithMetadata>

    @Query("SELECT * FROM oxygenSaturation WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getAllByRange(start: Instant, end: Instant): List<OxygenSaturationWithMetadata>

    @Query("SELECT * FROM oxygenSaturation WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    fun getFlowByRange(start: Instant, end: Instant): Flow<List<OxygenSaturationWithMetadata>>

    @Query("SELECT * FROM oxygenSaturation WHERE estimation_level IS NULL AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getWithoutEstimation(): List<OxygenSaturationWithMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OxygenSaturationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOxygenSaturation(entities: List<OxygenSaturationEntity>)

    @Transaction
    suspend fun insert(oxygenSaturationWithMetadata: OxygenSaturationWithMetadata) {
        insert(oxygenSaturationWithMetadata.oxygenSaturationEntity)
        insert(oxygenSaturationWithMetadata.resourceWithType)
        oxygenSaturationWithMetadata.noteEntity?.let { noteEntity -> insert(noteEntity) }
    }

    @Transaction
    suspend fun insertAllOxygenSaturationWithMetadata(oxygenSaturationWithMetadata: List<OxygenSaturationWithMetadata>) {
        insertAllNotes(entities = oxygenSaturationWithMetadata.mapNotNull { bloodPressureWithMetadata -> bloodPressureWithMetadata.noteEntity })
        insertAllResourcesWithType(resourcesWithType = oxygenSaturationWithMetadata.map { bloodPressureWithMetadata -> bloodPressureWithMetadata.resourceWithType })
        insertAllOxygenSaturation(entities = oxygenSaturationWithMetadata.map { bloodPressureWithMetadata -> bloodPressureWithMetadata.oxygenSaturationEntity })
    }
}
