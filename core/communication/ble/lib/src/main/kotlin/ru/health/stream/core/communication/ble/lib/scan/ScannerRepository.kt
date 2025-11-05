package ru.health.stream.core.communication.ble.lib.scan

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.retry
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import no.nordicsemi.ui.scanner.scanner.repository.DevicesDataStore
import ru.health.stream.core.monitor.Logger.logd
import ru.health.stream.core.monitor.Logger.logi
import ru.health.stream.core.monitor.Logger.logv
import ru.health.stream.core.monitor.Logger.logw

/**
 * Repository for scanning and tracking BLE devices
 *
 * This class manages BLE scanning operations, processes scan results, and tracks
 * discovered devices. It provides a Flow-based API that emits discovered devices
 * as they are found during scanning, with built-in error handling and retry logic
 *
 * The repository coordinates between the Bluetooth scanner and the device data store
 * to maintain a record of all discovered devices
 *
 * @property scanSettings settings that configure the BLE scan parameters
 * @property scanFilters list of filters to apply when scanning for devices
 * @property scanner the Bluetooth LE scanner implementation
 * @property devicesDataStore storage for discovered device information
 */
class ScannerRepository(
    private val scanSettings: ScanSettings,
    private val scanFilters: List<ScanFilter>,
    private val scanner: BluetoothLeScannerCompat,
    private val devicesDataStore: DevicesDataStore,
) {

    /**
     * Starts a BLE scan and returns a flow of discovered devices
     *
     * This method initiates a BLE scan with the configured settings and filters
     *
     * The scan continues until the Flow is collected or cancelled. The scanning will
     * automatically stop when collection ends
     *
     * @return Flow of discovered Bluetooth devices
     */
    fun startScan() = callbackFlow {
        logv("Start scanning using a callback")

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                logd("Emitting scanResult: $result")

                trackScan(scanResult = result)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                logw("onBatchScanResults received with ${results.size} results")

                results.forEach(::trackScan)
            }

            override fun onScanFailed(errorCode: Int) {
                logw("onScanFailed: $errorCode")
            }

            private fun trackScan(scanResult: ScanResult) {
                devicesDataStore.addNewDevice(scanResult = scanResult)

                val discoveredDevice = devicesDataStore.devices.first { device ->
                    device.matches(scanResult)
                }
                trySend(discoveredDevice)
            }
        }

        scanner.startScan(scanFilters, scanSettings, scanCallback)

        awaitClose {
            logi("Stopping Scan, channel closed")

            scanner.stopScan(scanCallback)
        }
    }.retry(retries = 3) { throwable ->
        // TODO: does this restart the entire flow?
        logw("Error, retrying device scan", throwable)

        true
    }.catch { throwable ->
        logw("Error performing device scan", throwable)

        emitAll(emptyFlow())
    }

    /**
     * Starts a BLE scan using a PendingIntent for background scanning
     *
     * @param context context required for permission checks and scan initialization
     * @param requestCode unique code to identify this scan request when results are delivered
     * @param pendingIntent PendingIntent that will receive scan results
     * @return Result object containing success or an exception if the scan could not be started
     * @throws SecurityException if running on Android 14+ and BLUETOOTH_SCAN permission is not granted
     */
    fun startScan(
        context: Context,
        requestCode: Int,
        pendingIntent: PendingIntent,
    ) {
        logv("Start scanning using pendingIntent. requestCode: $requestCode")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Missing BLUETOOTH_SCAN permission")
            }
        }

        scanner.startScan(scanFilters, scanSettings, context, pendingIntent, requestCode)
    }
}
