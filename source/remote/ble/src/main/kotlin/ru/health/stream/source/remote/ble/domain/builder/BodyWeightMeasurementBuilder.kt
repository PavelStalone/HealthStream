package ru.health.stream.source.remote.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket
import ru.health.stream.source.remote.ble.domain.device.TMB2048Packet
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SuppressLint("MissingPermission")
@OptIn(ExperimentalUuidApi::class)
internal class BodyWeightMeasurementBuilder(bluetoothDevice: BluetoothDevice) {

    private val device: Device = Device.WeightScale(
        id = bluetoothDevice.name,
        status = Device.Status.UNKNOWN,
        macAddress = bluetoothDevice.address,
        lastMeasured = Clock.System.now(),
    )

    private var lastMeasurePacket: GBS2012BPacket.Filled.WeightData? = null

    fun receive(packet: GBS2012BPacket.Filled) {
        if (packet is GBS2012BPacket.Filled.WeightData) lastMeasurePacket = packet
    }

    fun build(): BodyWeight? = lastMeasurePacket?.let { measure ->
        BodyWeight(
            id = Uuid.random().toString(),
            resource = device,
            weight = measure.weight.kg,
            createdAt = Clock.System.now(),
        )
    }
}
