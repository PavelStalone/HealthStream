package ru.health.stream.data.vitals.api.local

import ru.health.stream.data.vitals.model.Device

interface LocalDeviceSource {

    suspend fun getDeviceById(id: String): Result<Device>
    suspend fun <T : Device> writeDevice(device: T): Result<T>
}