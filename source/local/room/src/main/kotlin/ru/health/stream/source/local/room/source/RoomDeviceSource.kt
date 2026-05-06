package ru.health.stream.source.local.room.source

import ru.health.stream.data.vitals.model.Device
import ru.health.stream.source.infrastructure.source.local.LocalDeviceSource
import ru.health.stream.source.local.room.dao.DeviceDao
import ru.health.stream.source.local.room.entity.resource.DeviceEntity
import ru.health.stream.source.local.room.mapper.asDeviceEntityStatus
import ru.health.stream.source.local.room.mapper.asDeviceStatus
import javax.inject.Inject

internal class RoomDeviceSource @Inject constructor(
    private val deviceDao: DeviceDao,
) : LocalDeviceSource {

    override suspend fun getDeviceById(id: String): Result<Device> = runCatching {
        val entity = deviceDao.getDeviceById(id = id)

        when (entity.type) {
            DeviceEntity.DeviceType.PULSE_OXIMETER -> Device.PulseOximeter(
                id = entity.id,
                status = entity.status.asDeviceStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            DeviceEntity.DeviceType.WEIGHT_SCALE -> Device.WeightScale(
                id = entity.id,
                status = entity.status.asDeviceStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            DeviceEntity.DeviceType.BLOOD_PRESSURE_CUFF -> Device.BloodPressureCuff(
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
                type = DeviceEntity.DeviceType.PULSE_OXIMETER, // TODO: Change - shoplikpavel 2026-04-11
            )
        )

        return Result.success(device)
    }
}
