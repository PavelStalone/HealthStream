package ru.health.stream.room.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.health.stream.core.store.vitals.DeviceSource
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.room.dao.DeviceDao
import ru.health.stream.room.entity.DeviceEntity
import ru.health.stream.room.mapper.asStatus
import javax.inject.Inject

internal class RoomDeviceSource @Inject constructor(
    private val deviceDao: DeviceDao,
) : DeviceSource {

    override val isActive: Flow<Boolean> = flowOf(true)

    override suspend fun getDeviceById(id: String): Result<Device> = runCatching {
        val entity = deviceDao.getDeviceById(id = id)

        when (entity.type) {
            Device.PulseOximeter::class.simpleName -> Device.PulseOximeter(
                id = entity.id,
                status = entity.status.asStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            Device.WeightScale::class.simpleName -> Device.WeightScale(
                id = entity.id,
                status = entity.status.asStatus(),
                macAddress = entity.macAddress,
                lastMeasured = entity.lastMeasured,
            )

            Device.BloodPressure::class.simpleName -> Device.BloodPressure(
                id = entity.id,
                status = entity.status.asStatus(),
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
                status = device.status.asStatus(),
                lastMeasured = device.lastMeasured,
                type = device::class.simpleName.toString(),
            )
        )

        return Result.success(device)
    }
}
