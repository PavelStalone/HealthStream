package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.source.local.room.entity.BloodPressureEntity
import ru.health.stream.source.local.room.entity.BloodPressureWithMetadata

@Dao
internal interface BloodPressureDao : ResourceDao, NoteDao {

    @Query("SELECT * FROM bloodPressure WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getByRange(start: Instant, end: Instant): List<BloodPressureWithMetadata>

    @Query("SELECT * FROM bloodPressure WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getAllByRange(start: Instant, end: Instant): List<BloodPressureWithMetadata>

    @Query("SELECT * FROM bloodPressure WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    fun getFlowByRange(start: Instant, end: Instant): Flow<List<BloodPressureWithMetadata>>

    @Query("SELECT * FROM bloodPressure WHERE estimation_level IS NULL AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getWithoutEstimation(): List<BloodPressureWithMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodPressureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBloodPressure(entities: List<BloodPressureEntity>)

    @Transaction
    suspend fun insert(bloodPressureWithMetadata: BloodPressureWithMetadata) {
        insert(bloodPressureWithMetadata.bloodPressureEntity)
        insert(bloodPressureWithMetadata.resourceWithType)
        bloodPressureWithMetadata.noteEntity?.let { noteEntity -> insert(noteEntity) }
    }

    @Transaction
    suspend fun insertAllBloodPressureWithMetadata(bloodPressuresWithMetadata: List<BloodPressureWithMetadata>) {
        insertAllNotes(entities = bloodPressuresWithMetadata.mapNotNull { bloodPressureWithMetadata -> bloodPressureWithMetadata.noteEntity })
        insertAllResourcesWithType(resourcesWithType = bloodPressuresWithMetadata.map { bloodPressureWithMetadata -> bloodPressureWithMetadata.resourceWithType })
        insertAllBloodPressure(entities = bloodPressuresWithMetadata.map { bloodPressureWithMetadata -> bloodPressureWithMetadata.bloodPressureEntity })
    }
}
