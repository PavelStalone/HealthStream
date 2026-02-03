package ru.health.stream.core.store.vitals

import ru.health.stream.core.monitor.logD
import ru.health.stream.core.store.NotAvailableStoreException
import ru.health.stream.core.store.Store
import ru.health.stream.core.store.checkAvailable
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.source.local.LocalDeviceSource
import javax.inject.Inject

interface DeviceSource : Store, LocalDeviceSource

internal class LocalDeviceSourceImpl @Inject constructor(
    private val sources: Set<@JvmSuppressWildcards DeviceSource>
) : LocalDeviceSource {

    private var isChecked = false

    override suspend fun getDeviceById(id: String): Result<Device> {
        checkAvailable()

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .map { store -> store.getDeviceById(id = id) }
            .reduce { acc, result ->
                if (acc.isSuccess) return@reduce acc
                result
            }
    }

    override suspend fun <T : Device> writeDevice(device: T): Result<T> {
        checkAvailable()

        return sources.filter { store ->
            logD("${store::class.simpleName} store isActive: ${store.isActive()}")

            store.isActive()
        }
            .fold(initial = Result.failure(NotAvailableStoreException())) { acc, deviceStore ->
                val result = deviceStore.writeDevice(device = device)

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
