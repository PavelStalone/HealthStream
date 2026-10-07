package ru.health.stream.source.remote.ble.lib.scan

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import ru.health.stream.core.monitor.logV

class ScannerRepository(
    private val scanSettings: ScanSettings,
    private val scanFilters: List<ScanFilter>,
    private val scanner: BluetoothLeScannerCompat,
) {

    fun startScan(
        context: Context,
        requestCode: Int,
        pendingIntent: PendingIntent,
    ) {
        logV("Start scanning using broadcast receiver. requestCode: $requestCode")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Missing BLUETOOTH_SCAN permission")
            }
        }

        scanner.startScan(scanFilters, scanSettings, context, pendingIntent, requestCode)
    }

    fun stopScan(
        context: Context,
        requestCode: Int,
        pendingIntent: PendingIntent,
    ) {
        logV("Stop scanning using broadcast receiver. requestCode: $requestCode")

        scanner.stopScan(context, pendingIntent, requestCode)
    }
}
