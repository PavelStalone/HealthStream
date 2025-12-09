package ru.health.stream.room.store

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.core.store.measurement.HeartRateStore
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import ru.health.stream.room.dao.HeartRateDao
import ru.health.stream.room.entity.HeartRateEntity
import ru.health.stream.room.mapper.asEntity
import ru.health.stream.room.mapper.asSource
import javax.inject.Inject
import kotlin.time.Duration

internal class RoomHeartRateStore @Inject constructor(
    private val heartRateDao: HeartRateDao,
) : HeartRateStore {

    override suspend fun isActive(): Boolean = true

    override suspend fun getHeartRateByRange(start: Instant, end: Instant): List<HeartRate> =
        runCatching {
            logV("getHeartRateByRange called: start=$start, end=$end")

            val response = heartRateDao.getHeartRateByRange(start = start, end = end)
            logV("Founded heart rate entities: $response")

            response.map(HeartRateEntity::asSource)
        }.onFailure { exception ->
            logW(exception, "Error while getHeartRateByRange running")
        }.getOrElse { emptyList() }

    override suspend fun getHeartRateByDuration(duration: Duration): List<HeartRate> {
        logV("getHeartRateByDuration called: duration=$duration")

        val now = Clock.System.now()

        return getHeartRateByRange(start = now.minus(duration), end = now)
    }

    override suspend fun writeHeartRate(heartRate: HeartRate): Result<HeartRate> = runCatching {
        logV("writeHeartRate called: heartRate=$heartRate")

        heartRateDao.insertHeartRate(heartRate.asEntity())
        heartRate
    }.onFailure { exception ->
        logW(exception, "Error while writeHeartRate running")
    }
}