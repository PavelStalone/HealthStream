package ru.health.stream.core.communication.ble

import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import ru.health.stream.core.communication.ble.domain.BackgroundBleScanReceiver
import ru.health.stream.core.communication.ble.lib.scan.ScannerRepository
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logV

/**
 * Interface for managing BLE system components
 *
 * Provides methods to launch and manage BLE-related services and broadcast receivers
 * that handle device scanning and connection management. This interface abstracts the
 * system-level components required for BLE operation
 */
interface BleSystemManager {

    /**
     * Launches the BLE scanning service
     *
     * Starts the foreground service responsible for continuous BLE device scanning.
     * The service will continue scanning until explicitly stopped or system resources
     * are constrained
     *
     * @param context context required to start the service
     */
    fun launchScanService(context: Context)

    /**
     * Launches the BLE broadcast receiver
     *
     * Sets up a PendingIntent-based BLE scan that delivers results to a broadcast receiver.
     * This approach is suitable for background scanning when the app is not in the foreground
     *
     * @param context context required to register the broadcast receiver
     */
    fun launchBroadcastReceiver(context: Context)
}

@Singleton
internal class BleSystemManagerImpl @Inject constructor(
    private val scannerRepository: ScannerRepository,
) : BleSystemManager {

    override fun launchScanService(context: Context) {
        logV("launchService called")

        error("Not implemented")
    }

    override fun launchBroadcastReceiver(context: Context) {
        logV("launchBroadcastReceiver called")

        runCatching {
            BackgroundBleScanReceiver.createPendingIntentAndLaunch(
                context = context,
                scannerRepository = scannerRepository,
            )
        }.onFailure { throwable ->
            val message = "Failed to launch broadcast receiver for BLE scanning"

            logE(throwable, message)
        }
    }
}
