package ru.health.stream.source.remote.ble.lib.scan

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.device.NordicBleManager
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logI
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW

/**
 * Service for scanning and connecting to BLE devices
 *
 * Observes streams of discovered BLE devices, determines which ones are supported
 * by registered device profiles, and manages their connection lifecycle. Uses
 * the ActiveDeviceManager to prevent multiple simultaneous connections to the
 * same device
 *
 * @property context context used for BLE operations
 * @property bleDevices list of device profiles that define supported devices
 * @property coroutineScope scope for launching connection-related coroutines
 * @property coroutineDispatcher dispatcher for executing connection operations
 * @property deviceConnectionManager manager that tracks active device connections
 */
class ScanService(
    private val context: Context,
    private val bleDevices: List<BleDevice>,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val deviceConnectionManager: DeviceConnectionManager,
) {

    /**
     * Observes a flow of discovered BLE devices and processes each one
     *
     * @param flow stream of discovered Bluetooth devices to process
     * @param source identifier for the scan source, used for logging
     */
    suspend fun observe(flow: Flow<DiscoveredBluetoothDevice>, source: String) {
        logV("Starting device observation from source: $source")

        flow.collect { device -> handleDevice(device = device, source = source) }
    }

    /**
     * Handles a discovered device by checking if it's supported and connecting if possible
     *
     * The method attempts to register the connection with the ActiveDeviceManager,
     * finds a matching device profile, and initiates the connection process if
     * the device is supported and available for connection
     *
     * @param device the discovered Bluetooth device to handle
     * @param source identifier for the scan source, used for logging
     */
    fun handleDevice(device: DiscoveredBluetoothDevice, source: String) {
        logV("Processing device: ${device.name} from source: $source")

        coroutineScope.launch(coroutineDispatcher) {
            if (deviceConnectionManager.registerConnection(discoveredDevice = device)) {
                logI("Connection registered for device: ${device.name}")

                val deviceProfile = bleDevices
                    .firstOrNull { deviceProfile -> deviceProfile.isDeviceSupported(discoveredDevice = device) }

                if (deviceProfile != null) {
                    logD("Found matching profile for device: ${device.name}, profile: ${deviceProfile.javaClass.simpleName}")

                    NordicBleManager(
                        context = context,
                        bleDevice = deviceProfile,
                    ).apply {
                        launch {
                            logD("Starting connection state monitoring for device: ${device.name}")

                            stateAsFlow()
                                .drop(1) // always starts disconnected
                                .onCompletion {
                                    logI("Connection state flow completed for device: ${device.name}")
                                }
                                .collect { connectionState ->
                                    logD("Device ${device.name} state changed: ${connectionState.display()}")

                                    if (connectionState is ConnectionState.Disconnected &&
                                        !connectionState.isLinkLoss
                                    ) {
                                        specialClose()
                                        deviceConnectionManager.unregisterConnection(
                                            discoveredDevice = device
                                        )

                                        logD("Connection released for device: ${device.name}")
                                    }
                                }
                        }
                        launch {
                            logI("Initiating connection to device: ${device.name}")

                            specialConnect(device = device)
                        }
                    }
                } else {
                    logD("Not found profile for device: ${device.name}")

                    deviceConnectionManager.unregisterConnection(discoveredDevice = device)
                }
            }
        }
    }

    private suspend fun BleManager.specialConnect(device: DiscoveredBluetoothDevice) {
        logV("Attempting connection to device: ${device.name}")

        runCatching {
            connect(device.device)
                .before {
                    logI("Manager connect before to device: ${device.name}")
                }
                .done {
                    // Won't be called when using suspend
                    logI("Manager connect done to device: ${device.name}")
                }
                .then {
                    logI("Manager connect then to device: ${device.name}")
                }
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        }.onFailure { throwable ->
            logW("Connection failed for device: ${device.name}", throwable)

            specialClose()
        }
    }

    private fun BleManager.specialClose() {
        logD("Closing BLE manager")

        disconnect().enqueue()
        close()

        logD("BLE manager closed")
    }

    private fun ConnectionState.display(): String = when (this) {
        is ConnectionState.Ready -> "Ready"
        is ConnectionState.Connecting -> "Connecting"
        is ConnectionState.Initializing -> "Initializing"
        is ConnectionState.Disconnecting -> "Disconnecting"
        is ConnectionState.Disconnected -> "Disconnected, reason $reason"
    }
}
