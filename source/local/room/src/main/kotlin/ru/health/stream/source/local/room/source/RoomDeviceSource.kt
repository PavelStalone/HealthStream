package ru.health.stream.source.local.room.source

import ru.health.stream.data.vitals.api.local.LocalDeviceSource
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.source.local.room.dao.DeviceDao
import ru.health.stream.source.local.room.entity.DeviceEntity
import ru.health.stream.source.local.room.mapper.asDeviceEntityStatus
import ru.health.stream.source.local.room.mapper.asDeviceStatus
import javax.inject.Inject

internal class RoomDeviceSource @Inject constructor(
    private val deviceDao: DeviceDao,
) : LocalDeviceSource {

    override suspend fun getDeviceById(id: String): Result<Device> = runCatching {
        val entity = deviceDao.getDeviceById(id = id)

        when (entity.type) {
            Device.PulseOximeter::class.simpleName -> Device.PulseOximeter(
                id = entity.id,
                status = entity.status.asDeviceStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            Device.WeightScale::class.simpleName -> Device.WeightScale(
                id = entity.id,
                status = entity.status.asDeviceStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            Device.BloodPressure::class.simpleName -> Device.BloodPressure(
                id = entity.id,
                status = entity.status.asDeviceStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            else -> error("Not found device type")
        }
    }

    override suspend fun <T : Device> writeDevice(device: T): Result<T> {
        deviceDao.insert(
            DeviceEntity(
                id = device.id,
                macAddress = device.macAddress,
                status = device.status.asDeviceEntityStatus(),
                lastMeasured = device.lastMeasured,
                type = device::class.simpleName.toString(),
            )
        )

        return Result.success(device)
    }
}
