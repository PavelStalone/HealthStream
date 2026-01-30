package ru.health.stream.feature.vitals.source.remote.model

import ru.health.stream.feature.vitals.data.model.Device
import ru.health.stream.feature.vitals.data.model.SimpleHealthMeasurement

data class DeviceWithSimpleMeasurements(
    val resource: Device,
    val measurements: List<SimpleHealthMeasurement>,
)
