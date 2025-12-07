package ru.health.stream.core.store.heartrate

import kotlinx.datetime.Instant
import ru.health.stream.core.store.Store
import ru.health.stream.feature.vitals.source.local.LocalHeartRateStore
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import kotlin.time.Duration

interface HeartRateStore : Store, LocalHeartRateStore

internal class LocalHeartRateStoreImpl(
    private val sources: Set<HeartRateStore>
) : LocalHeartRateStore {

    override suspend fun getHeartRateByRange(from: Instant, to: Instant): List<HeartRate> {
        val d = sources.filter { store -> store.isActive() }
            .map { it.getHeartRateByRange(from = from, to = to) }
    }

    override suspend fun getHeartRateByDuration(duration: Duration): List<HeartRate> {
        TODO("Not yet implemented")
    }

    override suspend fun writeHeartRate(heartRate: HeartRate): Result<HeartRate> {
        TODO("Not yet implemented")
    }
}
