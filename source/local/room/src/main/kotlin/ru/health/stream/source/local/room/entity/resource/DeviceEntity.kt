package ru.health.stream.source.local.room.entity.resource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.source.local.room.mapper.asDeviceBloodPressureCuff
import ru.health.stream.source.local.room.mapper.asDeviceEntityStatus
import ru.health.stream.source.local.room.mapper.asDevicePulseOximeter
import ru.health.stream.source.local.room.mapper.asDeviceWeightScale

@Entity(tableName = "device")
internal data class DeviceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "type") val type: DeviceType,
    @ColumnInfo(name = "mac_address") val macAddress: String,
    @ColumnInfo(name = "last_measured") val lastMeasured: Instant,
    @ColumnInfo(name = "status") val status: Status,
) {

    fun asDevice(): Device = when (type) {
        DeviceType.WEIGHT_SCALE -> asDeviceWeightScale()
        DeviceType.PULSE_OXIMETER -> asDevicePulseOximeter()
        DeviceType.BLOOD_PRESSURE_CUFF -> asDeviceBloodPressureCuff()
    }

    enum class Status {

        UNKNOWN,
        ATTACHED,
        REJECTED,
        ;
    }

    enum class DeviceType {

        WEIGHT_SCALE,
        PULSE_OXIMETER,
        BLOOD_PRESSURE_CUFF,
        ;
    }
}

internal fun Device.asDeviceEntity(): DeviceEntity {
    val type = when (this) {
        is Device.BloodPressureCuff -> DeviceEntity.DeviceType.BLOOD_PRESSURE_CUFF
        is Device.PulseOximeter -> DeviceEntity.DeviceType.PULSE_OXIMETER
        is Device.WeightScale -> DeviceEntity.DeviceType.WEIGHT_SCALE
    }

    return DeviceEntity(
        id = id,
        type = type,
        macAddress = macAddress,
        lastMeasured = lastMeasured,
        status = status.asDeviceEntityStatus(),
    )
}
