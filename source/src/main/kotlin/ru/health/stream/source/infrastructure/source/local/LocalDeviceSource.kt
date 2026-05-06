package ru.health.stream.source.infrastructure.source.local

import ru.health.stream.data.vitals.model.Device

interface LocalDeviceSource {

    suspend fun getDeviceById(id: String): Result<Device>
    suspend fun <T : Device> writeDevice(device: T): Result<T>
}
