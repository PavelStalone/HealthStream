package ru.health.stream.feature.vitals.source.local

import ru.health.stream.feature.vitals.data.model.Device

interface LocalDeviceSource {

    suspend fun getDeviceById(id: String): Result<Device>
    suspend fun <T : Device> writeDevice(device: T): Result<T>
}
