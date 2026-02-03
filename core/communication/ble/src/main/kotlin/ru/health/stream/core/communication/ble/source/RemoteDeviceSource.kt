package ru.health.stream.core.communication.ble.source

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.health.stream.core.common.di.ApplicationCoroutineScope
import ru.health.stream.core.monitor.logV
import ru.health.stream.feature.vitals.source.remote.RemoteDeviceSource
import ru.health.stream.feature.vitals.source.remote.model.DeviceWithSimpleMeasurements

@Singleton
class RemoteDeviceSourceImpl @Inject constructor(
    @ApplicationCoroutineScope private val applicationScope: CoroutineScope,
) : RemoteDeviceSource {

    private val _flow = MutableSharedFlow<DeviceWithSimpleMeasurements>(
        replay = 0,
        extraBufferCapacity = 3,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override val flow: Flow<DeviceWithSimpleMeasurements> = _flow.asSharedFlow()

    internal fun sendMeasurements(measurements: DeviceWithSimpleMeasurements) {
        logV("sendMeasurements called: $measurements")

        applicationScope.launch { _flow.emit(measurements) }
    }
}
