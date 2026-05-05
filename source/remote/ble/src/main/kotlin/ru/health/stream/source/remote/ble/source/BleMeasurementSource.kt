package ru.health.stream.source.remote.ble.source

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
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.source.infrastructure.source.remote.RemoteMeasurementSource

@Singleton
internal class BleMeasurementSource @Inject constructor(
    @ApplicationCoroutineScope private val applicationScope: CoroutineScope,
) : RemoteMeasurementSource {

    private val _flow = MutableSharedFlow<Measurement>(
        replay = 0,
        extraBufferCapacity = 3,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override val flow: Flow<Measurement> = _flow.asSharedFlow()

    internal fun sendMeasurement(measurement: Measurement) {
        logV("sendMeasurement called: $measurement")

        applicationScope.launch { _flow.emit(measurement) }
    }
}
