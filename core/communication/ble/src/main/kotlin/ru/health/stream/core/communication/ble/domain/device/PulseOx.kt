package ru.health.stream.core.communication.ble.domain.device

import android.Manifest
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.communication.ble.domain.builder.PulseOxMeasurementBuilder
import ru.health.stream.core.communication.ble.lib.device.BleDevice
import ru.health.stream.core.communication.ble.lib.device.ConfigurationScope
import ru.health.stream.core.communication.ble.lib.device.buildScanFilter
import ru.health.stream.core.communication.ble.lib.device.createUUID
import ru.health.stream.core.communication.ble.lib.device.matchesAnyPrefix
import ru.health.stream.core.communication.ble.source.RemoteDeviceSourceImpl
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logV
import java.util.UUID

/**
 * BLE implementation for PC-60F pulse oximeter device
 */
internal class PulseOx(
    private val remoteDeviceSource: RemoteDeviceSourceImpl,
) : BleDevice() {

    private val DEVICE_NAMES = listOf("pc-60f")

    private val NORDIC_UART_SERVICE_UUID: UUID =
        createUUID(uuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_RX_CHARACTERISTIC_UUID: UUID =
        createUUID(uuid = "6e400002-b5a3-f393-e0a9-e50e24dcca9e") // writeNoResp / write
    private val UART_TX_CHARACTERISTIC_UUID: UUID =
        createUUID(uuid = "6e400003-b5a3-f393-e0a9-e50e24dcca9e") // notify

    private var measurementBuilder: PulseOxMeasurementBuilder? = null

    override val scanFilters: List<ScanFilter> = listOf(
        buildScanFilter { setServiceUuid(ParcelUuid(NORDIC_UART_SERVICE_UUID)) }
    )

    override fun ConfigurationScope.init() {
        service(uuid = NORDIC_UART_SERVICE_UUID) {
            characteristic(uuid = UART_TX_CHARACTERISTIC_UUID) {
                notificationCallback(
                    packet = PulsePacket.Definition,
                    callback = { pulse ->
                        logI("Pulse packet receive: $pulse")

                        measurementBuilder?.receive(pulse)
                    },
                )
            }

            characteristic(uuid = UART_RX_CHARACTERISTIC_UUID).run {
                writePacket(packet = PulsePacket.querySerialNumberCommand)
                writePacket(packet = PulsePacket.queryVersionCommand)
            }
        }
    }

    override fun onInvalidated() {
        logI("onInvalidated called")

        measurementBuilder?.build()
            ?.let { measurements -> remoteDeviceSource.sendMeasurements(measurements) }
        measurementBuilder = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun isDeviceSupported(discoveredDevice: DiscoveredBluetoothDevice): Boolean {
        logV("isDeviceSupported called: ${discoveredDevice.device.name}")

        return discoveredDevice.matchesAnyPrefix(prefixList = DEVICE_NAMES)
            .also { result ->
                if (result) measurementBuilder = PulseOxMeasurementBuilder(discoveredDevice.device)
            }
    }
}
