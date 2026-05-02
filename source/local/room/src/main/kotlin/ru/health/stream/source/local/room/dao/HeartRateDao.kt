package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.source.local.room.entity.HeartRateEntity
import ru.health.stream.source.local.room.entity.HeartRateWithMetadata

@Dao
internal interface HeartRateDao : ResourceDao, NoteDao {

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getByRange(start: Instant, end: Instant): List<HeartRateWithMetadata>

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    fun getFlowByRange(start: Instant, end: Instant): Flow<List<HeartRateWithMetadata>>

    @Query("SELECT * FROM heartRate WHERE estimation_level IS NULL ORDER BY created_at DESC")
    suspend fun getWithoutEstimation(): List<HeartRateWithMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HeartRateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHeartRates(entities: List<HeartRateEntity>)

    @Transaction
    suspend fun insert(heartRateWithMetadata: HeartRateWithMetadata) {
        insert(heartRateWithMetadata.heartRateEntity)
        insert(heartRateWithMetadata.resourceWithType)
        heartRateWithMetadata.noteEntity?.let { noteEntity -> insert(noteEntity) }
    }

    @Transaction
    suspend fun insertAllHeartRatesWithMetadata(heartRatesWithMetadata: List<HeartRateWithMetadata>) {
        insertAllNotes(entities = heartRatesWithMetadata.mapNotNull { heartRateWithMetadata -> heartRateWithMetadata.noteEntity })
        insertAllResourcesWithType(resourcesWithType = heartRatesWithMetadata.map { heartRateWithMetadata -> heartRateWithMetadata.resourceWithType })
        insertAllHeartRates(entities = heartRatesWithMetadata.map { heartRateWithMetadata -> heartRateWithMetadata.heartRateEntity })
    }
}
