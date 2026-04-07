package ru.health.stream.source.remote.ble.di

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import no.nordicsemi.ui.scanner.scanner.repository.DevicesDataStore
import ru.health.stream.core.common.di.ApplicationCoroutineScope
import ru.health.stream.core.common.di.Dispatcher
import ru.health.stream.core.starter.ActivityStarter
import ru.health.stream.source.remote.ble.BleSystemManager
import ru.health.stream.source.remote.ble.BleSystemManagerImpl
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.lib.scan.ScanService
import ru.health.stream.source.remote.ble.lib.scan.ScannerRepository
import ru.health.stream.source.remote.ble.lib.scan.TimeBasedDeviceConnectionManager

@Module
@InstallIn(SingletonComponent::class)
internal object BleModule {

    @Provides
    fun provideScanSettings(): ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setReportDelay(0)
        .setUseHardwareBatchingIfSupported(false)
        .build()

    @Provides
    fun provideBleScanFilters(
        devices: Set<@JvmSuppressWildcards BleDevice>,
    ): List<ScanFilter> = devices.flatMap(BleDevice::scanFilters)

    @Provides
    fun provideBleScanner() = BluetoothLeScannerCompat.getScanner()

    @Provides
    fun provideScannerRepository(
        scanSettings: ScanSettings,
        scanFilters: List<ScanFilter>,
        scanner: BluetoothLeScannerCompat,
        devicesDataStore: DevicesDataStore,
    ) = ScannerRepository(
        scanner = scanner,
        scanFilters = scanFilters,
        scanSettings = scanSettings,
        devicesDataStore = devicesDataStore,
    )

    @Provides
    fun provideScanService(
        devices: Set<@JvmSuppressWildcards BleDevice>,
        @ApplicationContext applicationContext: Context,
        @ApplicationCoroutineScope coroutineScope: CoroutineScope,
        @Dispatcher(Dispatcher.IO) coroutineDispatcher: CoroutineDispatcher,
    ) = ScanService(
        context = applicationContext,
        bleDevices = devices.toList(),
        coroutineScope = coroutineScope,
        coroutineDispatcher = coroutineDispatcher,
        deviceConnectionManager = TimeBasedDeviceConnectionManager,
    )

    @IntoSet
    @Provides
    fun provideBleScanStarter(
        bleSystemManager: BleSystemManager,
        @ApplicationContext context: Context,
    ) = object : ActivityStarter {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    bleSystemManager.launchBroadcastReceiver(context)
                }

                else -> {}
            }
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindsModule {

        @Binds
        fun bindBleSystemManager(impl: BleSystemManagerImpl): BleSystemManager
    }
}
