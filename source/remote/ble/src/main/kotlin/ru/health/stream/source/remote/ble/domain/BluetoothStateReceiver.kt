package ru.health.stream.source.remote.ble.domain

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.health.stream.core.common.di.ApplicationCoroutineScope
import ru.health.stream.source.remote.ble.AndroidBleSystemManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BluetoothStateReceiver @Inject constructor(
    @ApplicationCoroutineScope private val externalScope: CoroutineScope,
    private val bleManager: AndroidBleSystemManager
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    externalScope.launch {
                        bleManager.isBluetoothEnabled.emit(true)
                    }
                }

                BluetoothAdapter.STATE_OFF -> {
                    externalScope.launch {
                        bleManager.isBluetoothEnabled.emit(false)
                    }
                }
            }
        }
    }

    companion object {

        val IntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}
