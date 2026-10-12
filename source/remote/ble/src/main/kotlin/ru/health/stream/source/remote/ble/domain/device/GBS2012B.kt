package ru.health.stream.source.remote.ble.domain.device

import android.Manifest
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW
import ru.health.stream.source.remote.ble.domain.builder.GBS2012BMeasurementBuilder
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.BindingResponse
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.InitializationDeviceRequest
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.LoginDeviceRequest
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.RegistrationResponse
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.SettingCallback
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.Unknown
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.WeightData
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.device.ConfigurationScope
import ru.health.stream.source.remote.ble.lib.device.GattCharacteristic
import ru.health.stream.source.remote.ble.lib.device.buildScanFilter
import ru.health.stream.source.remote.ble.lib.device.createUUID
import ru.health.stream.source.remote.ble.lib.device.matchesAnyPrefix
import ru.health.stream.source.remote.ble.source.BleMeasurementSource

internal class GBS2012B(
    private val measurementSource: BleMeasurementSource,
) : BleDevice() {

    private var lastPacket: Filled? = null
    private var measurementBuilder: GBS2012BMeasurementBuilder? = null

    override val scanFilters: List<ScanFilter> = DEVICE_NAMES
        .flatMap { deviceName -> listOf(deviceName, deviceName.uppercase()) }
        .map { deviceName -> buildScanFilter { setDeviceName(deviceName) } }

    override fun ConfigurationScope.init() {
        service(uuid = DATA_SERVICE_UUID) {
            val ackTransfer = characteristic(uuid = ACK_TRANSFER_APP_TO_DEVICE)
            val commandTransfer = characteristic(uuid = COMMAND_TRANSFER_APP_TO_DEVICE)

            characteristic(uuid = ACK_TRANSFER_DEVICE_TO_APP) {
                notificationCallback(
                    packet = GBS2012BPacket.Definition,
                    callback = { packet ->
                        logI("notification packet in lxTransfer: $packet")

                        if (lastPacket == InitializationDeviceRequest) {
                            lastPacket = null
                            commandTransfer.writePacket(packet = GBS2012BPacket.synchronizeRequest)
                        }
                    }
                )
            }
            characteristic(uuid = NOTIFY_INFORMATION_TRANSFER_DEVICE_TO_APP) {
                notificationCallback(
                    packet = GBS2012BPacket.Definition,
                    callback = { packet ->
                        logI("notification packet: $packet")

                        lastPacket = packet
                        packet.handle(ackTransfer = ackTransfer, commandTransfer = commandTransfer)
                        measurementBuilder?.receive(packet)
                    }
                )
            }
            characteristic(uuid = INDICATE_INFORMATION_TRANSFER_DEVICE_TO_APP) {
                indicationCallback(
                    packet = GBS2012BPacket.Definition,
                    callback = { packet ->
                        logI("indication packet: $packet")

                        packet.handle(ackTransfer = ackTransfer, commandTransfer = commandTransfer)
                    }
                )
            }
        }
    }

    override fun onInvalidated() {
        logI("onInvalidated called")

        measurementBuilder?.build()
            ?.let { measurement -> measurementSource.sendMeasurement(measurement) }
        measurementBuilder = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun isDeviceSupported(discoveredDevice: DiscoveredBluetoothDevice): Boolean {
        logV("isDeviceSupported called: ${discoveredDevice.device.name}")

        return discoveredDevice.matchesAnyPrefix(prefixList = DEVICE_NAMES)
            .also { result ->
                if (result) measurementBuilder =
                    GBS2012BMeasurementBuilder(discoveredDevice.device)
            }
    }

    private fun Filled.handle(
        ackTransfer: GattCharacteristic,
        commandTransfer: GattCharacteristic,
    ) {
        when (this) {
            is BindingResponse -> {
                ackTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
                commandTransfer.writePacket(packet = GBS2012BPacket.synchronizeRequest)
            }

            is LoginDeviceRequest -> {
                ackTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
                commandTransfer.writePacket(
                    packet = GBS2012BPacket.loginResponse(verificationCode = verificationCode.toByteArray())
                )
            }

            InitializationDeviceRequest -> {
                ackTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
                commandTransfer.writePacket(packet = GBS2012BPacket.responseInitializationDevice)
            }

            is WeightData -> ackTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
            is SettingCallback -> ackTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
            RegistrationResponse -> ackTransfer.writePacket(packet = GBS2012BPacket.ackResponse)
            Unknown -> logW("Unknown packet")
        }
    }

    companion object {

        private val DEVICE_NAMES = listOf("gbs-2012-b", "gbf-2008-bf")

        private val DATA_SERVICE_UUID = createUUID("A602")
        private val ACK_TRANSFER_APP_TO_DEVICE = createUUID("A622")
        private val ACK_TRANSFER_DEVICE_TO_APP = createUUID("A625")
        private val COMMAND_TRANSFER_APP_TO_DEVICE = createUUID("A624")
        private val NOTIFY_INFORMATION_TRANSFER_DEVICE_TO_APP = createUUID("A621")
        private val INDICATE_INFORMATION_TRANSFER_DEVICE_TO_APP = createUUID("A620")
    }
}
