package ru.health.stream.feature.vitals.source.remote

import kotlinx.coroutines.flow.Flow
import ru.health.stream.feature.vitals.data.model.measurement.HealthMeasurement

interface RemoteDeviceSource {

    val flow: Flow<HealthMeasurement>
}
