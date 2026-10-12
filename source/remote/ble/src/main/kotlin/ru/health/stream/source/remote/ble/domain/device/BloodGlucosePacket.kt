package ru.health.stream.source.remote.ble.domain.device

import ru.health.stream.core.monitor.logE
import ru.health.stream.source.remote.ble.domain.device.BloodGlucosePacket.Filled.GlucoseMeasurement
import ru.health.stream.source.remote.ble.domain.device.BloodGlucosePacket.Filled.Unknown
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.TimeFilled
import ru.health.stream.source.remote.ble.lib.device.BlePacket
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.source.remote.ble.lib.packet.model.convertToPacket
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.Definition as TimeStampDefinition

internal object BloodGlucosePacket {

    /**
     * Definition for parsing blood glucose measurement data from the device according
     * to Bluetooth SIG Glucose Profile specification (0x2A18 Glucose Measurement).
     */
    object Definition : BlePacket.Definition<Filled>() {

        override val structure: PacketStructure
            get() = packet

        /**
         * Parses raw packet data into a structured blood glucose measurement
         *
         * @param rawPacket binary packet containing blood glucose measurement data
         * @return structured glucose data or Unknown if parsing fails
         */
        override fun parse(rawPacket: FilledPacket): Filled = runCatching {
            val unitIsMolL = rawPacket.findByName<Boolean>(UNIT_VALUE).value
            val sequenceNumber = rawPacket.findByName<Int>(SEQUENCE_NUMBER_VALUE).value

            val time = rawPacket.findByNameOrNull<ByteArray>(BASE_TIME_VALUE)
                ?.convertToPacket(TimeStampDefinition.structure)
                ?.let { packet -> TimeStampDefinition.parse(rawPacket = packet) as? TimeFilled.CurrentTime }

            val timeOffset = rawPacket.findByNameOrNull<Int>(TIME_OFFSET_VALUE)?.value

            val rawConcentration = rawPacket.findByNameOrNull<Float>(CONCENTRATION_VALUE)?.value

            // Convert to mmol/L:
            // If unit flag is 1 (mol/L): SFLOAT value in mol/L -> mmol/L = conc * 1000.0
            // If unit flag is 0 (kg/L): SFLOAT value in kg/L -> conc * 100000 / 18.0182 = mmol/L
            val concentrationInMmolL = rawConcentration?.let { conc ->
                if (unitIsMolL) {
                    conc * 1000.0f
                } else {
                    (conc * 100000.0f) / 18.0182f
                }
            }

            val typeAndLocation = rawPacket.findByNameOrNull<Int>(TYPE_AND_LOCATION_VALUE)?.value
            val sampleType = typeAndLocation?.and(0x0F)
            val sampleLocation = typeAndLocation?.shr(4)?.and(0x0F)

            val sensorStatus = rawPacket.findByNameOrNull<Int>(SENSOR_STATUS_VALUE)?.value

            return GlucoseMeasurement(
                sequenceNumber = sequenceNumber,
                time = time,
                timeOffsetMinutes = timeOffset,
                concentrationMmolL = concentrationInMmolL,
                unitIsMolL = unitIsMolL,
                sampleType = sampleType,
                sampleLocation = sampleLocation,
                sensorStatus = sensorStatus,
            )
        }.onFailure { throwable ->
            logE(throwable, "Error occurred while parsing Blood Glucose packet")
        }.getOrDefault(Unknown)

        /**
         * Structure definition for Bluetooth SIG Glucose Measurement format (0x2A18)
         */
        private val packet = PacketStructureBuilder.packetStructure {
            val unit = boolean(name = UNIT_VALUE)
            val statusPresent = boolean(name = STATUS_PRESENT_VALUE)
            val contextFollows = boolean(name = CONTEXT_FOLLOWS_VALUE)
            val timeOffsetPresent = boolean(name = TIME_OFFSET_PRESENT_VALUE)
            val concentrationPresent = boolean(name = CONCENTRATION_PRESENT_VALUE)

            section(name = "Flags") {
                reserve { 3.bits }
                include(contextFollows)
                include(statusPresent)
                include(unit)
                include(concentrationPresent)
                include(timeOffsetPresent)
            }

            section(name = "SequenceNumber") {
                int(name = SEQUENCE_NUMBER_VALUE, size = { 2.bytes })
            }

            section(name = "BaseTime") {
                bytes(name = BASE_TIME_VALUE, size = { 7.bytes })
            }

            section(name = "TimeOffset", isEnabled = { timeOffsetPresent.value }) {
                int(name = TIME_OFFSET_VALUE, size = { 2.bytes })
            }

            section(name = "GlucoseConcentration", isEnabled = { concentrationPresent.value }) {
                float(name = CONCENTRATION_VALUE)
                int(name = TYPE_AND_LOCATION_VALUE, size = { 1.bytes })
            }

            section(name = "SensorStatus", isEnabled = { statusPresent.value }) {
                int(name = SENSOR_STATUS_VALUE, size = { 2.bytes })
            }
        }

        private const val UNIT_VALUE = "unit"
        private const val BASE_TIME_VALUE = "baseTime"
        private const val TIME_OFFSET_VALUE = "timeOffset"
        private const val SENSOR_STATUS_VALUE = "sensorStatus"
        private const val CONCENTRATION_VALUE = "concentration"
        private const val STATUS_PRESENT_VALUE = "statusPresent"
        private const val CONTEXT_FOLLOWS_VALUE = "contextFollows"
        private const val SEQUENCE_NUMBER_VALUE = "sequenceNumber"
        private const val TYPE_AND_LOCATION_VALUE = "typeAndLocation"
        private const val TIME_OFFSET_PRESENT_VALUE = "timeOffsetPresent"
        private const val CONCENTRATION_PRESENT_VALUE = "concentrationPresent"
    }

    /**
     * Represents parsed blood glucose data from the device
     */
    sealed interface Filled : BlePacket.Data {

        /**
         * Contains a blood glucose measurement
         *
         * @property sampleType type of blood sample
         * @property sensorStatus optional sensor status flags
         * @property unitIsMolL true if raw unit flag was mol/L, false if kg/L
         * @property sequenceNumber measurement record sequence number
         * @property sampleLocation location on body where sample was taken
         * @property timeOffsetMinutes optional time offset in minutes
         * @property concentrationMmolL glucose concentration value in mmol/L
         * @property time optional timestamp when the measurement was taken
         */
        data class GlucoseMeasurement(
            val sampleType: Int?,
            val sensorStatus: Int?,
            val unitIsMolL: Boolean,
            val sequenceNumber: Int,
            val sampleLocation: Int?,
            val timeOffsetMinutes: Int?,
            val concentrationMmolL: Float?,
            val time: TimeFilled.CurrentTime?,
        ) : Filled

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : Filled
    }
}
