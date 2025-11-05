package ru.health.stream.core.communication.ble.lib.scan

import android.annotation.SuppressLint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.core.monitor.Logger.logd
import ru.health.stream.core.monitor.Logger.logi
import ru.health.stream.core.monitor.Logger.logv
import ru.health.stream.core.monitor.Logger.logw
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

/**
 * Manages active BLE device connections
 *
 * This interface provides methods to register and release device connections,
 * helping to prevent multiple simultaneous connections to the same device.
 * Implementations determine when devices can be reconnected based on their
 * specific connection management policies
 */
interface DeviceConnectionManager {

    /**
     * Registers a connection attempt with a device
     *
     * This method is called when a connection to a device is initiated.
     * It determines whether the connection should proceed based on the
     * current connection state of the device
     *
     * @param discoveredDevice the device to connect to
     * @return true if the connection should proceed, false if it should be prevented
     */
    fun registerConnection(discoveredDevice: DiscoveredBluetoothDevice): Boolean

    /**
     * Unregister a device connection
     *
     * This method is called when a connection to a device is terminated.
     * It removes the device from the active connections registry
     *
     * @param discoveredDevice the device whose connection is being released
     */
    fun unregisterConnection(discoveredDevice: DiscoveredBluetoothDevice)
}

/**
 * Time-based implementation of DeviceConnectionManager
 *
 * This implementation prevents reconnecting to devices that have been
 * connected recently, using a timeout period to determine when a device
 * can be reconnected. It helps prevent connection cycling and resource
 * contention when multiple components attempt to connect to the same device
 *
 * A device is considered "connected" for [connectedDeviceTimeout] (180) seconds
 * after a successful connection is registered. Subsequent connection attempts
 * within this period will be rejected
 */
val TimeBasedDeviceConnectionManager = object : DeviceConnectionManager {

    private val connectedDeviceTimeout = 180.seconds
    private val deviceConnectionTimeMap = ConcurrentHashMap<String, Instant>()

    override fun registerConnection(discoveredDevice: DiscoveredBluetoothDevice): Boolean {
        logv("registerConnection called: ${discoveredDevice.name}")

        val scanResult = discoveredDevice.lastScanResult ?: discoveredDevice.scanResult

        logd("Collected scan result: $scanResult")

        val bluetoothDevice = discoveredDevice.device

        @SuppressLint("MissingPermission")
        if (bluetoothDevice.name == null) {
            logw("Encountered device with null name")

            return false
        }

        /*
           Connected to device and store current time if device is not currently connected, or
           connection duration exceeds timeout
         */
        var shouldConnect = false
        deviceConnectionTimeMap.compute(bluetoothDevice.address) { address, connectedTime ->
            val currentTimestamp = Clock.System.now()

            if (connectedTime == null) {
                logi("Connecting to device $address for the first time")

                shouldConnect = true
                currentTimestamp
            } else {
                val connectedFor = currentTimestamp - connectedTime

                if (connectedFor > connectedDeviceTimeout) {
                    logw("Reconnecting to device $address, stale connection with duration: $connectedFor")

                    shouldConnect = true
                    currentTimestamp
                } else {
                    logi("Skip connecting to device $address, already connected for duration $connectedFor")

                    connectedTime
                }
            }
        }

        return shouldConnect
    }

    override fun unregisterConnection(discoveredDevice: DiscoveredBluetoothDevice) {
        logi("unregisterConnection called: ${discoveredDevice.device.address}")

        deviceConnectionTimeMap.remove(discoveredDevice.device.address)
    }
}
