package ru.health.stream.core.store.vitals

import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.store.NotAvailableStoreException
import ru.health.stream.core.store.Store
import ru.health.stream.core.store.checkAvailable
import ru.health.stream.core.store.mergeByTime
import ru.health.stream.feature.vitals.data.model.HealthMeasurement
import ru.health.stream.feature.vitals.source.local.LocalHealthMeasurementSource
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration

interface HealthMeasurementSource : Store, LocalHealthMeasurementSource

internal class LocalHealthMeasurementSourceImpl @Inject constructor(
    private val sources: Set<@JvmSuppressWildcards HealthMeasurementSource>
) : LocalHealthMeasurementSource {

    private var isChecked = false

    override suspend fun <T : HealthMeasurement> getMeasurementByRange(
        start: Instant,
        end: Instant,
        kClass: KClass<T>
    ): List<T> {
        logV("getMeasurementByRange called: start=$start, end=$end, kClass=$kClass")

        checkAvailable()

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .map { store -> store.getMeasurementByRange(start = start, end = end, kClass = kClass) }
            .mergeByTime(HealthMeasurement::createdAt)
            .toList()
    }

    override suspend fun <T : HealthMeasurement> getMeasurementByDuration(
        duration: Duration,
        kClass: KClass<T>
    ): List<T> {
        logV("getMeasurementByDuration called: duration=$duration, kClass=$kClass")

        checkAvailable()

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .map { store -> store.getMeasurementByDuration(duration = duration, kClass = kClass) }
            .mergeByTime(HealthMeasurement::createdAt)
            .toList()
    }

    override suspend fun <T : HealthMeasurement> writeMeasurement(
        measurement: T
    ): Result<T> {
        logV("writeMeasurement called: measurement=$measurement")

        checkAvailable()

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .fold(initial = Result.failure(NotAvailableStoreException())) { acc, healthMeasurementSource ->
                val result = healthMeasurementSource.writeMeasurement(measurement)

                if (acc.isSuccess) return@fold acc
                result
            }
    }

    private suspend fun checkAvailable() {
        if (isChecked) return

        sources.checkAvailable()
        isChecked = true
    }
}
