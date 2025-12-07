package ru.health.stream.feature.vitals.source.local

import kotlinx.datetime.Instant
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import kotlin.time.Duration

interface LocalHeartRateStore {

    suspend fun getHeartRateByRange(
        from: Instant,
        to: Instant,
    ): List<HeartRate>

    suspend fun getHeartRateByDuration(
        duration: Duration,
    ): List<HeartRate>

    suspend fun writeHeartRate(heartRate: HeartRate): Result<HeartRate>
}
