package ru.health.stream.source.remote.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.lb
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SuppressLint("MissingPermission")
@OptIn(ExperimentalUuidApi::class)
internal class GBS2012BMeasurementBuilder(bluetoothDevice: BluetoothDevice) {

    private val device: Device = Device.WeightScale(
        id = bluetoothDevice.address,
        name = bluetoothDevice.name,
        status = Device.Status.UNKNOWN,
        macAddress = bluetoothDevice.address,
        lastMeasured = Clock.System.now(),
    )

    private var lastMeasurePacket: GBS2012BPacket.Filled.WeightData? = null

    fun receive(packet: GBS2012BPacket.Filled) {
        if (packet is GBS2012BPacket.Filled.WeightData) lastMeasurePacket = packet
    }

    fun build(): BodyWeight? = lastMeasurePacket?.let { measure ->
        val weight = if (measure.unit) {
            (measure.weight * 0.01).lb
        } else {
            (measure.weight * 0.01).kg
        }

        BodyWeight(
            id = Uuid.random().toString(),
            resource = device,
            weight = weight,
            createdAt = Clock.System.now(),
        )
    }
}
