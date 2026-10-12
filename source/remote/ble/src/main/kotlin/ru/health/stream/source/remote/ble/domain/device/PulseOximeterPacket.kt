package ru.health.stream.source.remote.ble.domain.device

import ru.health.stream.core.monitor.logE
import ru.health.stream.source.remote.ble.domain.device.PulseOximeterPacket.Filled.Unknown
import ru.health.stream.source.remote.ble.lib.device.BlePacket
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes

/**
 * Defines packet structures and parsing logic for pulse oximeter devices
 */
internal object PulseOximeterPacket {

    object Definition : BlePacket.Definition<Filled>() {

        override val structure: PacketStructure
            get() = packet

        override fun parse(rawPacket: FilledPacket): Filled = runCatching {
            Filled.PulseOximeterData(
                spo2 = rawPacket.findByName<Float>(SPO2_VALUE).value,
                pulseRate = rawPacket.findByName<Float>(PULSE_RATE_VALUE).value
            )
        }.onFailure { throwable ->
            logE(throwable, "Error occurred while parsing packet")
        }.getOrDefault(Unknown)

        private val packet = PacketStructureBuilder.packetStructure {
            val slowDataFlag = boolean(name = SLOW_DATA_FLAG_VALUE)
            val fastDataFlag = boolean(name = FAST_DATA_FLAG_VALUE)
            val measurementStatusFlag = boolean(name = MEASUREMENT_STATUS_FLAG_VALUE)
            val pulseAmplitudeIndexFlag = boolean(name = PULSE_AMPLITUDE_INDEX_FLAG_VALUE)
            val deviceAndSensorStatusFlag = boolean(name = DEVICE_AND_SENSOR_STATUS_FLAG_VALUE)

            section(name = "Flags") {
                reserve { 2.bits }
                include(pulseAmplitudeIndexFlag)
                include(deviceAndSensorStatusFlag)
                include(measurementStatusFlag)
                include(slowDataFlag)
                include(fastDataFlag)
            }

            section(name = "NormalData") {
                float(name = SPO2_VALUE)
                float(name = PULSE_RATE_VALUE)
            }
            section(name = "FastData", isEnabled = { fastDataFlag.value }) {
                float(name = SPO2_FAST_VALUE)
                float(name = PULSE_RATE_FAST_VALUE)
            }
            section(name = "SlowData", isEnabled = { slowDataFlag.value }) {
                float(name = SPO2_SLOW_VALUE)
                float(name = PULSE_RATE_SLOW_VALUE)
            }
            section(name = "MeasurementStatus", isEnabled = { measurementStatusFlag.value }) {
                int(name = MEASUREMENT_STATUS_VALUE, size = { 2.bytes })
            }
            section(
                name = "DeviceAndSensorStatus",
                isEnabled = { deviceAndSensorStatusFlag.value },
            ) {
                int(name = DEVICE_AND_SENSOR_STATUS_VALUE, size = { 2.bytes })
            }
            section(name = "PulseAmplitudeIndex", isEnabled = { pulseAmplitudeIndexFlag.value }) {
                float(name = PULSE_AMPLITUDE_INDEX_VALUE)
            }
        }

        private const val SLOW_DATA_FLAG_VALUE = "slowDataFlag"
        private const val FAST_DATA_FLAG_VALUE = "fastDataFlag"
        private const val MEASUREMENT_STATUS_FLAG_VALUE = "measurementStatusFlag"
        private const val PULSE_AMPLITUDE_INDEX_FLAG_VALUE = "pulseAmplitudeIndexFlag"
        private const val DEVICE_AND_SENSOR_STATUS_FLAG_VALUE = "deviceAndSensorStatusFlag"

        private const val SPO2_VALUE = "spo2"
        private const val SPO2_FAST_VALUE = "spo2Fast"
        private const val SPO2_SLOW_VALUE = "spo2Slow"
        private const val PULSE_RATE_VALUE = "pulseRate"
        private const val PULSE_RATE_FAST_VALUE = "pulseRateFast"
        private const val PULSE_RATE_SLOW_VALUE = "pulseRateSlow"
        private const val MEASUREMENT_STATUS_VALUE = "measurementStatus"
        private const val PULSE_AMPLITUDE_INDEX_VALUE = "pulseAmplitudeIndex"
        private const val DEVICE_AND_SENSOR_STATUS_VALUE = "deviceAndSensorStatus"
    }

    sealed interface Filled : BlePacket.Data {

        data class PulseOximeterData(
            val spo2: Float,
            val pulseRate: Float,
        ) : Filled

        data object Unknown : Filled
    }
}
