package ru.health.stream.source.remote.ble.domain.device

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.monitor.logE
import ru.health.stream.source.remote.ble.domain.device.BloodPressurePacket.Filled.BloodPressureMeasurement
import ru.health.stream.source.remote.ble.domain.device.BloodPressurePacket.Filled.Unknown
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.TimeFilled
import ru.health.stream.source.remote.ble.lib.device.BlePacket
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.source.remote.ble.lib.packet.model.convertToPacket
import java.nio.ByteOrder
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.Definition as TimeStampDefinition

internal object BloodPressurePacket {

    /**
     * Packet for setting the device's current time
     *
     * Creates a command packet containing the current local time to synchronize
     * the device clock with the host device. Time is sent in components (year, month,
     * day, hour, minute, second) with flags indicating manual update from an external
     * time reference
     */
    val currentTime
        get() = BlePacket.filledPacket {
            val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            int(value = localDateTime.year, size = 2.bytes, name = "year")
            int(value = localDateTime.monthNumber, size = 1.bytes, name = "monthValue")
            int(value = localDateTime.dayOfMonth, size = 1.bytes, name = "dayOfMonth")
            int(value = localDateTime.hour, size = 1.bytes, name = "hour")
            int(value = localDateTime.minute, size = 1.bytes, name = "minute")
            int(value = localDateTime.second, size = 1.bytes, name = "second")
            int(value = localDateTime.dayOfWeek.value, size = 1.bytes, name = "dayOfWeek")
            // fractions 1/256 of a second
            int(value = 0, size = 1.bytes, name = "fractions")
            // manual update; external time reference update
            int(value = (1 or 2), size = 1.bytes, name = "mode")
        }

    /**
     * Definition for parsing blood pressure measurement data from the device
     *
     * This definition handles the device's measurement data format, which includes
     * systolic/diastolic pressures, pulse rate, measurement status flags, and optional
     * timestamp information
     */
    object Definition : BlePacket.Definition<Filled>() {

        override val structure: PacketStructure
            get() = packet

        /**
         * Parses raw packet data into a structured blood pressure measurement
         *
         * @param rawPacket binary packet containing blood pressure measurement data
         * @return structured blood pressure data or Unknown if parsing fails
         */
        override fun parse(rawPacket: FilledPacket): Filled = runCatching {
            val unit = rawPacket.findByName<Boolean>(UNIT_VALUE).value
            val systolic = rawPacket.findByName<Float>(SYSTOLIC_VALUE).value
            val diastolic = rawPacket.findByName<Float>(DIASTOLIC_VALUE).value
            val userId = rawPacket.findByNameOrNull<Int>(USER_ID_VALUE)?.value

            val meanArterialPressure =
                rawPacket.findByName<Float>(MEAN_ARTERIAL_PRESSURE_VALUE).value

            val time = rawPacket.findByNameOrNull<ByteArray>(TIMESTAMP_VALUE)
                ?.convertToPacket(TimeStampDefinition.structure)
                ?.let { packet -> TimeStampDefinition.parse(rawPacket = packet) as? TimeFilled.CurrentTime }

            val pulseRate = rawPacket.findByNameOrNull<Float>(PULSE_RATE_VALUE)?.value
            val measurementStatus = rawPacket.findByNameOrNull<Int>(MEASUREMENT_STATUS_VALUE)?.value
                ?.let { status -> BloodPressureMeasurement.BPMStatus.fromInt(status = status) }

            return BloodPressureMeasurement(
                time = time,
                userId = userId,
                systolic = systolic,
                diastolic = diastolic,
                pulseRate = pulseRate,
                status = measurementStatus,
                unit = 1.takeIf { unit } ?: 0,
                meanArterialPressure = meanArterialPressure,
            )
        }.onFailure { throwable ->
            logE(throwable, "Error occurred while parsing packet")
        }.getOrDefault(Unknown)

        /**
         * Structure definition for blood pressure measurement format
         */
        private val packet = PacketStructureBuilder.packetStructure {
            val userIdPresent = boolean(name = USER_ID_PRESENT_VALUE)
            val timestampPresent = boolean(name = TIMESTAMP_PRESENT_VALUE)
            val pulseRatePresent = boolean(name = PULSE_RATE_PRESENT_VALUE)
            val measurementStatusPresent = boolean(name = MEASUREMENT_STATUS_PRESENT_VALUE)

            section(name = "Flags") {
                reserve { 3.bits }
                include(measurementStatusPresent)
                include(userIdPresent)
                include(pulseRatePresent)
                include(timestampPresent)
                boolean(name = UNIT_VALUE)
            }

            section(name = "BloodPressure") {
                float(name = SYSTOLIC_VALUE)
                float(name = DIASTOLIC_VALUE)
                float(name = MEAN_ARTERIAL_PRESSURE_VALUE)
            }

            section(name = "Timestamp", isEnabled = { timestampPresent.value }) {
                bytes(name = TIMESTAMP_VALUE, size = { 7.bytes })
            }

            section(name = "PulseRate", isEnabled = { pulseRatePresent.value }) {
                float(name = PULSE_RATE_VALUE)
            }

            section(name = "UserId", isEnabled = { userIdPresent.value }) {
                int(name = USER_ID_VALUE, size = { 1.bytes })
            }

            section(name = "MeasurementStatus", isEnabled = { measurementStatusPresent.value }) {
                int(
                    name = MEASUREMENT_STATUS_VALUE,
                    byteOrder = ByteOrder.LITTLE_ENDIAN,
                    size = { 2.bytes },
                )
            }
        }

        private const val UNIT_VALUE = "unit"
        private const val USER_ID_VALUE = "userId"
        private const val SYSTOLIC_VALUE = "systolic"
        private const val DIASTOLIC_VALUE = "diastolic"
        private const val TIMESTAMP_VALUE = "timestamp"
        private const val PULSE_RATE_VALUE = "pulseRate"
        private const val USER_ID_PRESENT_VALUE = "userIdPresent"
        private const val TIMESTAMP_PRESENT_VALUE = "timestampPresent"
        private const val PULSE_RATE_PRESENT_VALUE = "pulseRatePresent"
        private const val MEASUREMENT_STATUS_VALUE = "measurementStatus"
        private const val MEAN_ARTERIAL_PRESSURE_VALUE = "meanArterialPressure"
        private const val MEASUREMENT_STATUS_PRESENT_VALUE = "measurementStatusPresent"
    }

    /**
     * Represents parsed blood pressure data from the device
     */
    sealed interface Filled : BlePacket.Data {

        /**
         * Contains a complete blood pressure measurement
         *
         * @property unit measurement unit (0 = mmHg, 1 = kPa)
         * @property userId optional user identifier the measurement is associated with
         * @property systolic systolic pressure value
         * @property diastolic diastolic pressure value
         * @property pulseRate optional pulse rate in beats per minute
         * @property status optional measurement status flags
         * @property meanArterialPressure mean arterial pressure value
         * @property time optional timestamp when the measurement was taken
         */
        data class BloodPressureMeasurement(
            val unit: Int,
            val userId: Int?,
            val systolic: Float,
            val diastolic: Float,
            val pulseRate: Float?,
            val status: BPMStatus?,
            val meanArterialPressure: Float,
            val time: TimeFilled.CurrentTime?,
        ) : Filled {

            /**
             * Status flags for blood pressure measurement
             *
             * Contains various flags indicating measurement quality and conditions
             *
             * @property cuffTooLose whether the cuff was detected as too loose
             * @property pulseRateInRange whether the pulse rate is within normal range
             * @property bodyMovementDetected whether body movement was detected during measurement
             * @property irregularPulseDetected whether irregular pulse was detected
             * @property pulseRateExceedsUpperLimit whether pulse rate exceeds upper limit
             * @property improperMeasurementPosition whether improper position was detected
             * @property pulseRateIsLessThenLowerLimit whether pulse rate is below lower limit
             */
            data class BPMStatus(
                val cuffTooLose: Boolean,
                val pulseRateInRange: Boolean,
                val bodyMovementDetected: Boolean,
                val irregularPulseDetected: Boolean,
                val pulseRateExceedsUpperLimit: Boolean,
                val improperMeasurementPosition: Boolean,
                val pulseRateIsLessThenLowerLimit: Boolean,
            ) {

                companion object {

                    /**
                     * Creates a status object from packed integer bit flags
                     *
                     * @param status integer containing bit flags
                     * @return parsed BPMStatus object
                     */
                    fun fromInt(status: Int): BPMStatus = BPMStatus(
                        cuffTooLose = (status and 0x02) != 0,
                        bodyMovementDetected = (status and 0x01) != 0,
                        pulseRateInRange = (status and 0x18) shr 3 == 0,
                        irregularPulseDetected = (status and 0x04) != 0,
                        improperMeasurementPosition = (status and 0x20) != 0,
                        pulseRateExceedsUpperLimit = (status and 0x18) ushr 3 == 1,
                        pulseRateIsLessThenLowerLimit = (status and 0x18) ushr 3 == 2,
                    )
                }
            }
        }

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : Filled
    }
}
