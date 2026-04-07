package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.health.stream.source.local.room.entity.HeartRateEntity

@Dao
internal interface HeartRateDao : MeasurementDao<HeartRateEntity> {

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    override suspend fun getByRange(start: Instant, end: Instant): List<HeartRateEntity>

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    override fun getFlowByRange(start: Instant, end: Instant): Flow<List<HeartRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: HeartRateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<HeartRateEntity>)
}
