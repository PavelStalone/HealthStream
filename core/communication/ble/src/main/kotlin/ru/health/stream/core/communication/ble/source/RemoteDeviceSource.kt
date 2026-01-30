package ru.health.stream.core.communication.ble.source

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.health.stream.feature.vitals.source.remote.RemoteDeviceSource
import ru.health.stream.feature.vitals.source.remote.model.DeviceWithSimpleMeasurements

class RemoteDeviceSourceImpl @Inject constructor() : RemoteDeviceSource {

    override val flow: Flow<DeviceWithSimpleMeasurements> = flow {}
}
