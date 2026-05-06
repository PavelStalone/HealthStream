package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.source.local.room.entity.BodyWeightEntity
import ru.health.stream.source.local.room.entity.BodyWeightWithMetadata

@Dao
internal interface BodyWeightDao : ResourceDao, NoteDao {

    @Query("SELECT * FROM bodyWeight WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getByRange(start: Instant, end: Instant): List<BodyWeightWithMetadata>

    @Query("SELECT * FROM bodyWeight WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getAllByRange(start: Instant, end: Instant): List<BodyWeightWithMetadata>

    @Query("SELECT * FROM bodyWeight WHERE created_at >= :start AND created_at <= :end AND is_removed == FALSE ORDER BY created_at DESC")
    fun getFlowByRange(start: Instant, end: Instant): Flow<List<BodyWeightWithMetadata>>

    @Query("SELECT * FROM bodyWeight WHERE estimation_level IS NULL AND is_removed == FALSE ORDER BY created_at DESC")
    suspend fun getWithoutEstimation(): List<BodyWeightWithMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BodyWeightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBodyWeight(entities: List<BodyWeightEntity>)

    @Transaction
    suspend fun insert(bodyWeightWithMetadata: BodyWeightWithMetadata) {
        insert(bodyWeightWithMetadata.bodyWeightEntity)
        insert(bodyWeightWithMetadata.resourceWithType)
        bodyWeightWithMetadata.noteEntity?.let { noteEntity -> insert(noteEntity) }
    }

    @Transaction
    suspend fun insertAllBodyWeightWithMetadata(bodyWeightWithMetadata: List<BodyWeightWithMetadata>) {
        insertAllNotes(entities = bodyWeightWithMetadata.mapNotNull { bloodPressureWithMetadata -> bloodPressureWithMetadata.noteEntity })
        insertAllResourcesWithType(resourcesWithType = bodyWeightWithMetadata.map { bloodPressureWithMetadata -> bloodPressureWithMetadata.resourceWithType })
        insertAllBodyWeight(entities = bodyWeightWithMetadata.map { bloodPressureWithMetadata -> bloodPressureWithMetadata.bodyWeightEntity })
    }
}
