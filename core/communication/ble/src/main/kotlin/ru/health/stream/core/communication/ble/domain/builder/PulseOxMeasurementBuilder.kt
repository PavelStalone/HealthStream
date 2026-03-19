package ru.health.stream.core.communication.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.core.communication.ble.domain.device.PulsePacket
import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.data.model.measurement.HeartRate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SuppressLint("MissingPermission")
@OptIn(ExperimentalUuidApi::class)
internal class PulseOxMeasurementBuilder(bluetoothDevice: BluetoothDevice) {

    private val device: Device = Device.PulseOximeter(
        id = bluetoothDevice.name,
        status = Device.Status.UNKNOWN,
        macAddress = bluetoothDevice.address,
        lastMeasured = Clock.System.now(),
    )

    private var lastMeasurePacket: PulsePacket.Filled.SpoParameterPacket? = null

    fun receive(packet: PulsePacket.Filled) {
        if (packet is PulsePacket.Filled.SpoParameterPacket) lastMeasurePacket = packet
    }

    fun build(): HeartRate? = lastMeasurePacket?.let { measure ->
        HeartRate(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = device,
            pulse = measure.pulse,
        )
    }
}
