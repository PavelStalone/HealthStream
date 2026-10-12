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
import ru.health.stream.source.remote.ble.domain.builder.BloodPressureMeasurementBuilder
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.device.ConfigurationScope
import ru.health.stream.source.remote.ble.lib.device.buildScanFilter
import ru.health.stream.source.remote.ble.lib.device.createUUID
import ru.health.stream.source.remote.ble.source.BleMeasurementSource
import java.util.UUID
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.Definition as TimeStampDefinition

/**
 * BLE implementation for TMB2084 blood pressure monitor device
 */
internal class BloodPressureDevice(
    private val measurementSource: BleMeasurementSource,
) : BleDevice() {

    private var measurementBuilder: BloodPressureMeasurementBuilder? = null

    override val scanFilters: List<ScanFilter> = listOf(
        buildScanFilter { setServiceUuid(ParcelUuid(MEASUREMENT_SERVICE_UUID)) }
    )

    override fun ConfigurationScope.init() {
        service(uuid = Services.CURRENT_TIME.uuid) {
            characteristic(uuid = Characteristics.CURRENT_TIME.uuid) {
                notificationCallback(
                    packet = TimeStampDefinition,
                    callback = { time ->
                        logI("Notification CurrentTime: $time")
                    },
                )
            }.run {
                writePacket(packet = BloodPressurePacket.currentTime)
                readPacket(packet = TimeStampDefinition) { time ->
                    logI("Read packet: $time")
                }
            }
        }
        service(uuid = MEASUREMENT_SERVICE_UUID) {
            characteristic(MEASUREMENT_UUID) {
                indicationCallback(
                    packet = BloodPressurePacket.Definition,
                    callback = { packet ->
                        logI("Read packet: $packet")

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
        val hasUUID = scanRecord?.serviceUuids?.contains(ParcelUuid(MEASUREMENT_SERVICE_UUID))
        if (hasUUID != true) return false

        measurementBuilder = BloodPressureMeasurementBuilder(discoveredDevice.device)
        return true
    }

    companion object {

        private val MEASUREMENT_UUID = createUUID("2A35")
        private val MEASUREMENT_SERVICE_UUID: UUID = createUUID("1810")
    }
}
