package ru.health.stream.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.datetime.Instant
import ru.health.stream.room.entity.HeartRateEntity

@Dao
internal interface HeartRateDao {

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end")
    suspend fun getHeartRateByRange(start: Instant, end: Instant): List<HeartRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(heartRateEntity: HeartRateEntity)
}
