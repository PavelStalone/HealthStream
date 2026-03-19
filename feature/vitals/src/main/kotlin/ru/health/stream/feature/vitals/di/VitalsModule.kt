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
import ru.health.stream.feature.vitals.data.model.Device
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
                remoteDeviceSource.flow.collect { measurement ->
                    logV("Measurement found: $measurement")

                    val device = measurement.resource as? Device

                    // Save or update connected device
                    device?.let { device ->
                        val localDevice = localDeviceSource.getDeviceById(device.id)
                            .getOrDefault(device)
                            .copy(lastMeasured = device.lastMeasured)

                        localDeviceSource.writeDevice(localDevice)
                    }

                    localHealthMeasurement.writeMeasurement(measurement)
                }
            }
        }
    }
}
