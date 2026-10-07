package ru.health.stream.source.remote.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.health.stream.core.common.di.ApplicationCoroutineScope
import ru.health.stream.core.common.permission.Permission
import ru.health.stream.core.common.permission.PermissionManager
import ru.health.stream.core.common.permission.PermissionStatus
import ru.health.stream.core.monitor.logE
import ru.health.stream.core.monitor.logV
import ru.health.stream.source.local.KeyValueSource
import ru.health.stream.source.remote.ble.domain.BackgroundBleScanReceiver
import ru.health.stream.source.remote.ble.lib.scan.ScannerRepository
import javax.inject.Inject
import javax.inject.Singleton

interface BleSystemManager {

    fun startScan()
    fun stopScan()

    fun enableBluetooth(context: Context)

    val isScanning: Flow<BluetoothStatus>
}

@Singleton
internal class AndroidBleSystemManager @Inject constructor(
    private val keyValueSource: KeyValueSource,
    private val scannerRepository: ScannerRepository,
    private val permissionManager: PermissionManager,
    @ApplicationContext private val context: Context,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
) : BleSystemManager {

    private val isScanningNow = MutableStateFlow(false)
    val isBluetoothEnabled = MutableStateFlow(isBluetoothEnabled())

    init {
        coroutineScope.launch {
            val isScanning = runCatching {
                require(BackgroundBleScanReceiver.isPendingActive(context))
                requireNotNull(keyValueSource.getValue<Boolean>(SCANNING_KEY))
            }.getOrDefault(false)

            isScanningNow.emit(isScanning)
        }
    }

    override val isScanning: Flow<BluetoothStatus> = combine(
        isBluetoothEnabled,
        isScanningNow
    ) { isBluetoothEnabled, isScanningNow ->
        if (isBluetoothEnabled) {
            if (isScanningNow) BluetoothStatus.Scanning else BluetoothStatus.Idle
        } else {
            BluetoothStatus.Disabled
        }
    }

    override fun startScan() {
        logV("startScan called")

        coroutineScope.launch {
            val result = permissionManager.requestGroup(
                Permission.BluetoothConnect,
                Permission.BluetoothScan
            )

            if (result.all { (_, status) -> status == PermissionStatus.Granted }) {
                runCatching {
                    BackgroundBleScanReceiver.createPendingIntentAndLaunch(
                        context = context,
                        scannerRepository = scannerRepository,
                    )
                }.onSuccess {
                    isScanningNow.emit(true)
                    keyValueSource.saveValue(key = SCANNING_KEY, true)
                }.onFailure { throwable ->
                    val message = "Failed to start BLE scanning"

                    logE(throwable, message)
                }
            }
        }
    }

    override fun stopScan() {
        logV("stopScan called")

        coroutineScope.launch {
            runCatching {
                BackgroundBleScanReceiver.stop(
                    context = context,
                    scannerRepository = scannerRepository,
                )
            }.onSuccess {
                isScanningNow.emit(false)
                keyValueSource.saveValue(key = SCANNING_KEY, false)
            }.onFailure { throwable ->
                val message = "Failed to stop BLE scanning"

                logE(throwable, message)
            }
        }
    }

    @androidx.annotation.RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun enableBluetooth(context: Context) {
        coroutineScope.launch {
            val result = permissionManager.request(Permission.BluetoothConnect)

            if (result == PermissionStatus.Granted) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

                (context as? Activity)?.run {
                    startActivityForResult(intent, 1)
                    return@launch
                }
                context.startActivity(intent)
            }
        }
    }

    private fun isBluetoothEnabled(): Boolean = runCatching {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        bluetoothAdapter?.isEnabled == true
    }.getOrDefault(false)

    companion object {

        const val SCANNING_KEY = "IsScanningActiveKey"
    }
}

enum class BluetoothStatus {

    Idle,
    Scanning,
    Disabled,
    ;
}
