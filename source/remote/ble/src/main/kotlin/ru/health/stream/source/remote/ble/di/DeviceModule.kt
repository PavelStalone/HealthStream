package ru.health.stream.source.remote.ble.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import ru.health.stream.source.remote.ble.domain.device.GBS2012B
import ru.health.stream.source.remote.ble.domain.device.PulseOx
import ru.health.stream.source.remote.ble.domain.device.TMB2084
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
        PulseOx(measurementSource = measurementSource),
        TMB2084(),
        GBS2012B(),
    )
}
