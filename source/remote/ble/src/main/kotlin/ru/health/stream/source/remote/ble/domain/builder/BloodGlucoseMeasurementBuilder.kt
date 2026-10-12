package ru.health.stream.source.remote.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.source.remote.ble.domain.device.BloodGlucosePacket
import ru.health.stream.source.remote.ble.domain.device.BloodGlucosePacket.Filled.GlucoseMeasurement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SuppressLint("MissingPermission")
@OptIn(ExperimentalUuidApi::class)
internal class BloodGlucoseMeasurementBuilder(bluetoothDevice: BluetoothDevice) {

    private val device: Device = Device.GlucoseMeter(
        id = bluetoothDevice.address,
        name = bluetoothDevice.name,
        status = Device.Status.UNKNOWN,
        macAddress = bluetoothDevice.address,
        lastMeasured = Clock.System.now(),
    )

    private var lastMeasurePacket: GlucoseMeasurement? = null

    fun receive(packet: BloodGlucosePacket.Filled) {
        if (packet is GlucoseMeasurement) lastMeasurePacket = packet
    }

    fun build(): BloodGlucose? = lastMeasurePacket?.concentrationMmolL?.let { level ->
        BloodGlucose(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = device,
            level = level.toDouble(),
        )
    }
}
