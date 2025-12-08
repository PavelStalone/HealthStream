package ru.health.stream.core.store.heartrate

import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.store.NotAvailableStoreException
import ru.health.stream.core.store.Store
import ru.health.stream.core.store.mergeByTime
import ru.health.stream.feature.vitals.source.local.LocalHeartRateStore
import ru.health.stream.feature.vitals.source.local.model.HeartRate
import javax.inject.Inject
import kotlin.time.Duration

interface HeartRateStore : Store, LocalHeartRateStore

internal class LocalHeartRateStoreImpl @Inject constructor(
    private val sources: Set<@JvmSuppressWildcards HeartRateStore>
) : LocalHeartRateStore {

    override suspend fun getHeartRateByRange(
        start: Instant,
        end: Instant,
    ): List<HeartRate> {
        logV("getHeartRateByRange called: from=$start, to=$end")

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .map { store -> store.getHeartRateByRange(start = start, end = end) }
            .mergeByTime(HeartRate::createdAt)
            .toList()
    }

    override suspend fun getHeartRateByDuration(
        duration: Duration,
    ): List<HeartRate> {
        logV("getHeartRateByRange called: duration=$duration")

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .map { store -> store.getHeartRateByDuration(duration = duration) }
            .mergeByTime(HeartRate::createdAt)
            .toList()
    }

    override suspend fun writeHeartRate(
        heartRate: HeartRate,
    ): Result<HeartRate> {
        logV("writeHeartRate called: heartRate=$heartRate")

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .fold(initial = Result.failure(NotAvailableStoreException())) { acc, heartRateStore ->
                val result = heartRateStore.writeHeartRate(heartRate)

                if (acc.isSuccess) return@fold acc
                result
            }
    }
}
