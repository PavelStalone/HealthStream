package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.source.local.room.entity.BloodGlucoseEntity
import ru.health.stream.source.local.room.entity.BloodGlucoseWithMetadata

@Dao
internal interface BloodGlucoseDao : ResourceDao, NoteDao {

    @Query("SELECT * FROM bloodGlucose WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getByRange(start: Instant, end: Instant): List<BloodGlucoseWithMetadata>

    @Query("SELECT * FROM bloodGlucose WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getAllByRange(start: Instant, end: Instant): List<BloodGlucoseWithMetadata>

    @Query("SELECT * FROM bloodGlucose WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    fun getFlowByRange(start: Instant, end: Instant): Flow<List<BloodGlucoseWithMetadata>>

    @Query("SELECT * FROM bloodGlucose WHERE estimation_level IS NULL AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getWithoutEstimation(): List<BloodGlucoseWithMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodGlucoseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBloodGlucose(entities: List<BloodGlucoseEntity>)

    @Transaction
    suspend fun insert(bloodGlucoseWithMetadata: BloodGlucoseWithMetadata) {
        insert(bloodGlucoseWithMetadata.bloodGlucoseEntity)
        insert(bloodGlucoseWithMetadata.resourceWithType)
        bloodGlucoseWithMetadata.noteEntity?.let { noteEntity -> insert(noteEntity) }
    }

    @Transaction
    suspend fun insertAllBloodGlucoseWithMetadata(bloodGlucoseWithMetadata: List<BloodGlucoseWithMetadata>) {
        insertAllNotes(entities = bloodGlucoseWithMetadata.mapNotNull { item -> item.noteEntity })
        insertAllResourcesWithType(resourcesWithType = bloodGlucoseWithMetadata.map { item -> item.resourceWithType })
        insertAllBloodGlucose(entities = bloodGlucoseWithMetadata.map { item -> item.bloodGlucoseEntity })
    }
}
