package ru.health.stream.source.infrastructure.source.remote

import kotlinx.coroutines.flow.Flow
import ru.health.stream.data.vitals.model.measurement.Measurement

interface RemoteMeasurementSource {

    val flow: Flow<Measurement>
}
