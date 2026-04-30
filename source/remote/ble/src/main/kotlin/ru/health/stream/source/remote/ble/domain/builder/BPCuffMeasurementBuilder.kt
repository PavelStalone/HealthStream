package ru.health.stream.source.remote.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.source.remote.ble.domain.device.TMB2048Packet
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SuppressLint("MissingPermission")
@OptIn(ExperimentalUuidApi::class)
internal class BPCuffMeasurementBuilder(bluetoothDevice: BluetoothDevice) {

    private val device: Device = Device.BloodPressureCuff(
        id = bluetoothDevice.name,
        status = Device.Status.UNKNOWN,
        macAddress = bluetoothDevice.address,
        lastMeasured = Clock.System.now(),
    )

    private var lastMeasurePacket: TMB2048Packet.Filled.BloodPressureMeasurement? = null

    fun receive(packet: TMB2048Packet.Filled) {
        if (packet is TMB2048Packet.Filled.BloodPressureMeasurement) lastMeasurePacket = packet
    }

    fun build(): BloodPressure? = lastMeasurePacket?.let { measure ->
        BloodPressure(
            id = Uuid.random().toString(),
            createdAt = Clock.System.now(),
            resource = device,
            systolic = measure.systolic,
            diastolic = measure.diastolic,
        )
    }
}
