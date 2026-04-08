package ru.health.stream.source.remote.ble.domain

import android.app.PendingIntent
import android.bluetooth.le.BluetoothLeScanner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import no.nordicsemi.ui.scanner.toDiscoveredBluetoothDevice
import ru.health.stream.core.common.di.ApplicationCoroutineScope
import ru.health.stream.core.monitor.logD
import ru.health.stream.source.remote.ble.lib.scan.ScanService
import ru.health.stream.source.remote.ble.lib.scan.ScannerRepository
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundBleScanReceiver : BroadcastReceiver() {

    @Inject
    @ApplicationCoroutineScope
    lateinit var externalScope: CoroutineScope

    @Inject
    internal lateinit var scanService: ScanService

    override fun onReceive(context: Context, intent: Intent) {
        val callbackType = intent.getIntExtra(
            BluetoothLeScanner.EXTRA_CALLBACK_TYPE,
            ScanSettings.CALLBACK_TYPE_ALL_MATCHES
        )

        val errorCode = intent.getIntExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE, 0)
        val scanResult: ArrayList<ScanResult>? = intent.getParcelableArrayListExtra(
            BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT,
            ScanResult::class.java
        )

        logD("onReceive with callbackType $callbackType, errorCode $errorCode, scanResults $scanResult")

        externalScope.launch {
            scanResult?.forEach { result ->
                val device = result.toDiscoveredBluetoothDevice()

                scanService.handleDevice(
                    device = device,
                    source = SOURCE_NAME,
                )
            }
        }
    }

    companion object {

        private const val REQUEST_CODE = 12085
        private const val SOURCE_NAME = "BackgroundBleScanReceiver"
        private const val INTENT_ACTION = "ru.health.stream.ble.ACTION_FOUND"

        /**
         * Creates and launches a PendingIntent for background BLE scanning
         *
         * This method configures a broadcast receiver PendingIntent that will be triggered
         * when BLE devices are discovered during background scanning
         *
         * @param context Android context used to create the PendingIntent
         * @param scannerRepository repository to initiate the background BLE scan
         * @throws RuntimeException if the scanning cannot be started
         */
        internal fun createPendingIntentAndLaunch(
            context: Context,
            scannerRepository: ScannerRepository,
        ) {
            val intent = Intent(context, BackgroundBleScanReceiver::class.java).apply {
                action = INTENT_ACTION
                setPackage(context.packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            scannerRepository.startScan(
                context = context,
                requestCode = REQUEST_CODE,
                pendingIntent = pendingIntent,
            )
        }
    }
}
