package ru.health.stream.source.remote.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.source.remote.ble.domain.device.PulsePacket
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

    fun build(): Pair<HeartRate, OxygenSaturation>? = lastMeasurePacket?.let { measure ->
        val createdAt = Clock.System.now()

        Pair(
            HeartRate(
                id = Uuid.random().toString(),
                createdAt = createdAt,
                resource = device,
                pulse = measure.pulse,
            ),
            OxygenSaturation(
                id = Uuid.random().toString(),
                createdAt = createdAt,
                resource = device,
                saturation = measure.spoData.toFloat()
            )
        )
    }
}
