package ru.health.stream.source.remote.ble.domain.device

import android.Manifest
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import com.movisens.smartgattlib.Characteristics
import com.movisens.smartgattlib.Services
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logV
import ru.health.stream.source.remote.ble.domain.builder.BloodGlucoseMeasurementBuilder
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.device.ConfigurationScope
import ru.health.stream.source.remote.ble.lib.device.buildScanFilter
import ru.health.stream.source.remote.ble.lib.device.createUUID
import ru.health.stream.source.remote.ble.source.BleMeasurementSource
import java.util.UUID
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.Definition as TimeStampDefinition

internal class BloodGlucoseDevice(
    private val measurementSource: BleMeasurementSource,
) : BleDevice() {

    private var measurementBuilder: BloodGlucoseMeasurementBuilder? = null

    override val scanFilters: List<ScanFilter> = listOf(
        buildScanFilter { setServiceUuid(ParcelUuid(DATA_SERVICE_UUID)) }
    )

    override fun ConfigurationScope.init() {
        service(uuid = DATA_SERVICE_UUID) {
            characteristic(DATA_UUID) {
                notificationCallback(
                    packet = BloodGlucosePacket.Definition,
                    callback = { packet ->
                        logI("Glucose packet received: $packet")

                        measurementBuilder?.receive(packet)
                    },
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

        val scanRecord = with(discoveredDevice) { (lastScanResult ?: scanResult)?.scanRecord }
        val hasUUID = scanRecord?.serviceUuids?.contains(ParcelUuid(DATA_SERVICE_UUID))
        if (hasUUID != true) return false

        measurementBuilder = BloodGlucoseMeasurementBuilder(discoveredDevice.device)
        return true
    }

    companion object {

        private val DATA_UUID = createUUID("2A18")
        private val DATA_SERVICE_UUID: UUID = createUUID("1808")
    }
}
