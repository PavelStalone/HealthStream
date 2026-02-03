package ru.health.stream.feature.vitals.source.remote

import kotlinx.coroutines.flow.Flow
import ru.health.stream.feature.vitals.source.remote.model.DeviceWithSimpleMeasurements

interface RemoteDeviceSource {

    val flow: Flow<DeviceWithSimpleMeasurements>
}
