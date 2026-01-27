package ru.health.stream.core.communication.ble.domain.device

import android.icu.util.Calendar
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.communication.ble.domain.device.TMB2048Packet.Filled.BloodPressureMeasurement
import ru.health.stream.core.communication.ble.domain.device.TMB2048Packet.Filled.Unknown
import ru.health.stream.core.communication.ble.lib.device.BlePacket
import ru.health.stream.core.communication.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacket
import ru.health.stream.core.communication.ble.lib.packet.model.PacketStructure
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.core.communication.ble.lib.packet.model.convertToPacket
import ru.health.stream.core.monitor.logE
import java.nio.ByteOrder

/**
 * BLE packet definitions for TMB2048 blood pressure monitor device
 *
 * This object provides the packet structures and parsing logic for communicating with TMB2048
 * series blood pressure monitors over Bluetooth Low Energy
 */
internal object TMB2048Packet {

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
     * Definition for parsing time information from blood pressure monitor packets
     *
     * This definition handles the device's timestamp format which includes
     * year, month, day, hour, minute, and second values. It converts these
     * components into Calendar and Instant objects for easier use in the application
     */
    object TimeDefinition : BlePacket.Definition<TimeFilled>() {

        override val structure: PacketStructure
            get() = packet

        /**
         * Parses raw packet data into a structured time representation
         *
         * @param rawPacket binary packet containing time information
         * @return structured time data or Unknown if parsing fails
         */
        override fun parse(rawPacket: FilledPacket): TimeFilled {
            val calendar = Calendar.getInstance()

            val year = rawPacket.findByName<Int>(name = YEAR_VALUE).value
            val month = rawPacket.findByName<Int>(name = MONTH_VALUE).value
            val day = rawPacket.findByName<Int>(name = DAY_VALUE).value
            val hour = rawPacket.findByName<Int>(name = HOUR_VALUE).value
            val minute = rawPacket.findByName<Int>(name = MINUTE_VALUE).value
            val second = rawPacket.findByName<Int>(name = SECOND_VALUE).value

            if (year > 0) calendar[Calendar.YEAR] = year
            else calendar.clear(Calendar.YEAR)

            // months are 1-based in Date Time characteristic
            if (month > 0) calendar[Calendar.MONTH] = month - 1
            else calendar.clear(Calendar.MONTH)

            if (day > 0) calendar[Calendar.DATE] = day
            else calendar.clear(Calendar.DATE)

            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            calendar[Calendar.SECOND] = second
            calendar[Calendar.MILLISECOND] = 0

            if (calendar != null) return TimeFilled.CurrentTime(
                instant = Instant.fromEpochMilliseconds(calendar.timeInMillis)
            )
            return TimeFilled.Unknown
        }

        /**
         * Structure definition for time data format
         */
        private val packet = PacketStructureBuilder.packetStructure {
            section(name = "Time") {
                int(name = YEAR_VALUE, size = { 2.bytes }, byteOrder = ByteOrder.LITTLE_ENDIAN)
                int(name = MONTH_VALUE, size = { 1.bytes })
                int(name = DAY_VALUE, size = { 1.bytes })
                int(name = HOUR_VALUE, size = { 1.bytes })
                int(name = MINUTE_VALUE, size = { 1.bytes })
                int(name = SECOND_VALUE, size = { 1.bytes })
            }
        }

        private const val DAY_VALUE = "day"
        private const val HOUR_VALUE = "hour"
        private const val YEAR_VALUE = "year"
        private const val MONTH_VALUE = "month"
        private const val MINUTE_VALUE = "minute"
        private const val SECOND_VALUE = "second"
    }

    /**
     * Represents parsed time data from blood pressure monitor
     */
    sealed interface TimeFilled : BlePacket.Data {

        /**
         * Contains a successfully parsed timestamp
         *
         * @property instant instant representation of the timestamp
         */
        data class CurrentTime(val instant: Instant) : TimeFilled

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : TimeFilled
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
                ?.convertToPacket(TimeDefinition.structure)
                ?.let { packet -> TimeDefinition.parse(rawPacket = packet) as? TimeFilled.CurrentTime }

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
