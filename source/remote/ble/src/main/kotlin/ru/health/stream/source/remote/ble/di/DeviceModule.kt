package ru.health.stream.source.remote.ble.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import ru.health.stream.source.remote.ble.domain.device.BloodGlucoseDevice
import ru.health.stream.source.remote.ble.domain.device.BloodPressureDevice
import ru.health.stream.source.remote.ble.domain.device.GBS2012B
import ru.health.stream.source.remote.ble.domain.device.PC60FDevice
import ru.health.stream.source.remote.ble.domain.device.PulseOximeterDevice
import ru.health.stream.source.remote.ble.domain.device.WeightScaleDevice
import ru.health.stream.source.remote.ble.lib.device.BleDevice
import ru.health.stream.source.remote.ble.source.BleMeasurementSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DeviceModule {

    @Provides
    @Singleton
    @ElementsIntoSet
    fun provideBleDevices(
        measurementSource: BleMeasurementSource
    ): Set<BleDevice> = setOf(
        GBS2012B(measurementSource = measurementSource),
        PC60FDevice(measurementSource = measurementSource),
        WeightScaleDevice(measurementSource = measurementSource),
        BloodGlucoseDevice(measurementSource = measurementSource),
        BloodPressureDevice(measurementSource = measurementSource),
        PulseOximeterDevice(measurementSource = measurementSource),
    )
}
