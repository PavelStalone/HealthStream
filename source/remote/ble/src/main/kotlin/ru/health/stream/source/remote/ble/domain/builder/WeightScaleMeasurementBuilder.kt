package ru.health.stream.source.remote.ble.domain.builder

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.datetime.Clock
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.lb
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.source.remote.ble.domain.device.WeightScalePacket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@SuppressLint("MissingPermission")
@OptIn(ExperimentalUuidApi::class)
internal class WeightScaleMeasurementBuilder(bluetoothDevice: BluetoothDevice) {

    private val device: Device = Device.WeightScale(
        id = bluetoothDevice.address,
        name = bluetoothDevice.name,
        status = Device.Status.UNKNOWN,
        macAddress = bluetoothDevice.address,
        lastMeasured = Clock.System.now(),
    )

    private var lastMeasurePacket: WeightScalePacket.Filled.WeightScaleData? = null

    fun receive(packet: WeightScalePacket.Filled) {
        if (packet is WeightScalePacket.Filled.WeightScaleData) lastMeasurePacket = packet
    }

    fun build(): BodyWeight? = lastMeasurePacket?.let { measure ->
        val weight = if (measure.unit) {
            (measure.weight * KG_MULTIPLE).kg
        } else {
            (measure.weight * LB_MULTIPLE).lb
        }

        BodyWeight(
            id = Uuid.random().toString(),
            weight = weight,
            resource = device,
            createdAt = Clock.System.now(),
        )
    }

    companion object {

        private const val LB_MULTIPLE = 0.01
        private const val KG_MULTIPLE = 0.005
    }
}
