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
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.device.ConfigurationScope
import ru.health.stream.source.remote.ble.lib.device.buildScanFilter
import ru.health.stream.source.remote.ble.lib.device.matchesAnyPrefix

/**
 * BLE implementation for TMB2084 blood pressure monitor device
 */
class TMB2084 : BleDevice() {

    private val DEVICE_NAMES = listOf("tmb_2084_a", "tmb-2084-a")

    override val scanFilters: List<ScanFilter> = listOf(
        buildScanFilter { setServiceUuid(ParcelUuid(Services.BLOOD_PRESSURE.uuid)) }
    )

    override fun ConfigurationScope.init() {
        service(uuid = Services.CURRENT_TIME.uuid) {
            characteristic(uuid = Characteristics.CURRENT_TIME.uuid) {
                notificationCallback(
                    packet = TMB2048Packet.TimeDefinition,
                    callback = { time ->
                        logI("Notification CurrentTime: $time")
                    },
                )
            }.run {
                writePacket(packet = TMB2048Packet.currentTime)
                readPacket(packet = TMB2048Packet.TimeDefinition) { time ->
                    logI("Read packet: $time")
                }
            }
        }
        service(uuid = Services.BLOOD_PRESSURE.uuid) {
            characteristic(Characteristics.BLOOD_PRESSURE_MEASUREMENT.uuid) {
                indicationCallback(
                    packet = TMB2048Packet.Definition,
                    callback = { packet ->
                        logI("Read packet: $packet")
                    },
                )
            }
        }
    }

    override fun onInvalidated() {
        logI("onInvalidated called")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun isDeviceSupported(discoveredDevice: DiscoveredBluetoothDevice): Boolean {
        logV("isDeviceSupported called: ${discoveredDevice.device.name}")

        return discoveredDevice.matchesAnyPrefix(prefixList = DEVICE_NAMES)
    }
}
