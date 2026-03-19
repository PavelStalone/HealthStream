package ru.health.stream.room.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface MeasurementDao<E> {

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end")
    suspend fun getByRange(start: Instant, end: Instant): List<E>

    @Query("SELECT * FROM heartRate WHERE created_at >= :start AND created_at <= :end")
    fun getFlowByRange(start: Instant, end: Instant): Flow<List<E>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: E)
}
