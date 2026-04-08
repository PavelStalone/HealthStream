package ru.health.stream.data.vitals.api.remote

import kotlinx.coroutines.flow.Flow
import ru.health.stream.data.vitals.model.measurement.Measurement

interface RemoteMeasurementSource {

    val flow: Flow<Measurement>
}
