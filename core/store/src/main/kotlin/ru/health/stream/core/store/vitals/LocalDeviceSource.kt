package ru.health.stream.core.store.vitals

import kotlinx.coroutines.flow.first
import ru.health.stream.core.common.bindByFlow
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.store.NotAvailableStoreException
import ru.health.stream.core.store.Store
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.source.local.LocalDeviceSource
import javax.inject.Inject

interface DeviceSource : Store, LocalDeviceSource

internal class LocalDeviceSourceImpl @Inject constructor(
    sources: Set<@JvmSuppressWildcards DeviceSource>
) : LocalDeviceSource {

    private val activeSources = sources.bindByFlow { item -> item.isActive }

    override suspend fun getDeviceById(id: String): Result<Device> {

        return activeSources.first()
            .map { source ->
                logD("${source::class.simpleName} isActive")

                source.getDeviceById(id = id)
            }
            .reduce { acc, result ->
                if (acc.isSuccess) return@reduce acc
                result
            }
    }

    override suspend fun <T : Device> writeDevice(device: T): Result<T> {

        return activeSources.first()
            .fold(initial = Result.failure(NotAvailableStoreException())) { acc, deviceSource ->
                val sourceName = deviceSource::class.simpleName
                logD("$sourceName isActive")

                val result = deviceSource.writeDevice(device = device)

                logD("$sourceName write: ${result.isSuccess}")

                if (acc.isSuccess) return@fold acc
                result
            }
    }
}
