package ru.health.stream.feature.vitals.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.health.stream.core.common.di.ApplicationCoroutineScope
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.starter.AppStarter
import ru.health.stream.feature.vitals.data.model.addResource
import ru.health.stream.feature.vitals.data.model.copy
import ru.health.stream.feature.vitals.source.local.LocalDeviceSource
import ru.health.stream.feature.vitals.source.local.LocalHealthMeasurementSource
import ru.health.stream.feature.vitals.source.remote.RemoteDeviceSource

@Module
@InstallIn(SingletonComponent::class)
internal object VitalsModule {

    @IntoSet
    @Provides
    fun provideVitalsMeasurementsObserver(
        localDeviceSource: LocalDeviceSource,
        remoteDeviceSource: RemoteDeviceSource,
        localHealthMeasurement: LocalHealthMeasurementSource,
        @ApplicationCoroutineScope applicationScope: CoroutineScope,
    ) = object : AppStarter {

        override fun onCreate() {
            applicationScope.launch {
                remoteDeviceSource.flow.collect { deviceWithMeasurements ->
                    val (device, measurements) = deviceWithMeasurements

                    logV("Device found: $device, measurements: $measurements")

                    // Save or update connected device
                    val localDevice = localDeviceSource.getDeviceById(device.id)
                        .getOrDefault(device)
                        .copy(id = device.id, lastMeasured = device.lastMeasured)
                    localDeviceSource.writeDevice(localDevice)

                    measurements.forEach { measurement ->

                        localHealthMeasurement.writeMeasurement(measurement.addResource(localDevice))
                    }
                }
            }
        }
    }
}
