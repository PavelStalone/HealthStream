package ru.health.stream.source.remote.ble.domain.device

import android.Manifest
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logV
import ru.health.stream.source.remote.ble.domain.builder.PulseOximeterMeasurementBuilder
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.device.ConfigurationScope
import ru.health.stream.source.remote.ble.lib.device.buildScanFilter
import ru.health.stream.source.remote.ble.lib.device.createUUID
import ru.health.stream.source.remote.ble.source.BleMeasurementSource
import java.util.UUID

internal class PulseOximeterDevice(
    private val measurementSource: BleMeasurementSource,
) : BleDevice() {

    private var measurementBuilder: PulseOximeterMeasurementBuilder? = null

    override val scanFilters: List<ScanFilter> = listOf(
        buildScanFilter { setServiceUuid(ParcelUuid(DATA_SERVICE_UUID)) }
    )

    override fun ConfigurationScope.init() {
        service(uuid = DATA_SERVICE_UUID) {
            characteristic(uuid = DATA_UUID) {
                notificationCallback(
                    packet = PulseOximeterPacket.Definition,
                    callback = { pulse ->
                        logI("Pulse packet receive: $pulse")

                        measurementBuilder?.receive(pulse)
                    },
                )
            }
        }
    }

    override fun onInvalidated() {
        logI("onInvalidated called")

        measurementBuilder?.build()
            ?.let { (heartRate, oxygenSaturation) ->
                measurementSource.sendMeasurement(heartRate)
                measurementSource.sendMeasurement(oxygenSaturation)
            }
        measurementBuilder = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun isDeviceSupported(discoveredDevice: DiscoveredBluetoothDevice): Boolean {
        logV("isDeviceSupported called: ${discoveredDevice.device.name}")

        val scanRecord = with(discoveredDevice) { (lastScanResult ?: scanResult)?.scanRecord }
        val hasUUID = scanRecord?.serviceUuids?.contains(ParcelUuid(DATA_SERVICE_UUID))
        if (hasUUID != true) return false

        measurementBuilder = PulseOximeterMeasurementBuilder(discoveredDevice.device)
        return true
    }

    companion object {

        private val DATA_UUID: UUID = createUUID(uuid = "2A5F")
        private val DATA_SERVICE_UUID: UUID = createUUID(uuid = "1822")
    }
}
