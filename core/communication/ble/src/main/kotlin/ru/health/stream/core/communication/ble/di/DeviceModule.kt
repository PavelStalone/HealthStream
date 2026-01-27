package ru.health.stream.core.communication.ble.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import ru.health.stream.core.communication.ble.domain.device.GBS2012B
import ru.health.stream.core.communication.ble.domain.device.PulseOx
import ru.health.stream.core.communication.ble.domain.device.TMB2084
import ru.health.stream.core.communication.ble.lib.device.BleDevice

@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {

    @Provides
    @ElementsIntoSet
    fun provideBleDevices(): Set<BleDevice> = setOf(
        PulseOx(),
        TMB2084(),
        GBS2012B(),
    )
}
