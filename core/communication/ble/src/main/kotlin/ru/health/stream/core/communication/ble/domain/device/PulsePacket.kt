package ru.health.stream.core.communication.ble.domain.device

import ru.health.stream.core.communication.ble.domain.device.PulsePacket.Filled.BatteryPower.BatteryLevel
import ru.health.stream.core.communication.ble.domain.device.PulsePacket.Filled.*
import ru.health.stream.core.communication.ble.domain.device.PulsePacket.Filled.StatusData.SpotCheckMeasurement.*
import ru.health.stream.core.communication.ble.lib.device.BlePacket
import ru.health.stream.core.communication.ble.lib.device.byteArrayOfInts
import ru.health.stream.core.communication.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacket
import ru.health.stream.core.communication.ble.lib.packet.model.FilledValue
import ru.health.stream.core.communication.ble.lib.packet.model.PacketStructure
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.core.communication.ble.lib.packet.model.convertToPacket
import ru.health.stream.core.communication.ble.lib.packet.model.map
import ru.health.stream.core.communication.ble.lib.packet.validation.base.CRC8Validation
import ru.health.stream.core.communication.ble.lib.packet.validation.base.SizeValidation
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logE
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Defines packet structures and parsing logic for pulse oximeter devices
 */
internal object PulsePacket {

    /**
     * Command packet to query device firmware and hardware version information
     */
    val queryVersionCommand = BlePacket.filledPacket {
        val productInfo = bytes(
            value = byteArrayOfInts(0xAA, 0x55, 0xF0, 0x02, 0x81),
            name = "productInfo",
        )
        byte(value = CRC8Validation.calculate(productInfo))
    }

    /**
     * Command packet to query device serial number
     */
    val querySerialNumberCommand = BlePacket.filledPacket {
        val serial = bytes(
            value = byteArrayOfInts(0xAA, 0x55, 0xF0, 0x02, 0x82),
            name = "serial",
        )
        byte(value = CRC8Validation.calculate(serial))
    }

    /**
     * Creates a command packet to enable or disable SpO2 parameter transmission
     *
     * @param enabled true to enable parameter transmission, false to disable
     */
    fun enableSendingSpoParameters(enabled: Boolean) = BlePacket.filledPacket {
        val spoParameter = bytes(
            value = byteArrayOfInts(0xAA, 0x55, 0x0F, 0x03, 0x84),
            name = "spoParameter",
        )
        val command = boolean(
            value = enabled,
            size = 1.bytes,
            name = "isEnabled",
        )
        byte(value = CRC8Validation.calculate(spoParameter, command))
    }

    /**
     * Creates a command packet to enable or disable SpO2 waveform transmission
     *
     * @param enabled true to enable waveform transmission, false to disable
     */
    fun enableSendingSpoWaveform(enabled: Boolean) = BlePacket.filledPacket {
        val spoWaveform = bytes(
            value = byteArrayOfInts(0xAA, 0x55, 0x0F, 0x03, 0x85),
            name = "spoWaveform",
        )
        val command = boolean(
            value = enabled,
            size = 1.bytes,
            name = "isEnabled",
        )
        byte(value = CRC8Validation.calculate(spoWaveform, command))
    }

    /**
     * Definition for parsing raw packets received from pulse oximeter devices
     *
     * This object defines the packet structure and provides parsing logic to convert
     * raw binary data into structured data models representing different types of
     * device responses
     */
    object Definition : BlePacket.Definition<Filled>() {

        override val structure: PacketStructure
            get() = packet

        override fun parse(rawPacket: FilledPacket): Filled = runCatching {
            logD("parse called: $rawPacket")

            val token = rawPacket.findByName<Int>(name = TOKEN_VALUE).value
            val dataType = rawPacket.findByName<Int>(name = TYPE_VALUE).value
            val content = rawPacket.findByName<ByteArray>(name = CONTENT_VALUE)

            when (token) {
                0xF0 -> {
                    when (dataType) {
                        0x01 -> return ProductInfo.extractFrom(content = content)
                        0x02 -> return SerialNumber.extractFrom(content = content)
                        0x03 -> return BatteryPower.extractFrom(content = content)
                    }
                }

                0x0F -> {
                    when (dataType) {
                        0x04 -> return FrequencySpoParameters.extractFrom(content = content)
                        0x05 -> return FrequencySpoWaveform.extractFrom(content = content)
                        0x01 -> return SpoParameterPacket.extractFrom(content = content)
                        0x02 -> return SpoWaveformData.extractFrom(content = content)
                        0x21 -> return StatusData.extractFrom(content = content)
                    }
                }
            }

            return Unknown
        }.onFailure { throwable ->
            logE(throwable, "Error occurred while parsing packet")
        }.getOrDefault(Unknown)

        /**
         * Defines the binary structure of the pulse oximeter communication protocol
         */
        private val packet = PacketStructureBuilder.packetStructure {
            val headSection = section(name = "Head") {
                int(name = HEAD_VALUE, size = { 2.bytes })
            }
            val tokenSection = section(name = "Token") {
                int(name = TOKEN_VALUE, size = { 1.bytes })
            }

            val length = int(name = LENGTH_VALUE, size = { 1.bytes })
            val lengthSection = section(name = "Length") {
                include(valueStructure = length)
            }

            val typeSection = section(name = "Type") {
                int(name = TYPE_VALUE, size = { 1.bytes })
            }
            val contentSection = section(name = "Content") {
                bytes(name = CONTENT_VALUE, size = { (length.value - 2).bytes })
            }

            val crc = int(name = CRC_VALUE, size = { 1.bytes })
            val crcSection = section(name = "CRC") {
                include(valueStructure = crc)
            }

            validation(
                validation = SizeValidation(size = { length.value.bytes }),
                sections = listOf(typeSection, contentSection, crcSection),
            )
            validation(
                validation = CRC8Validation(crc = { crc.value.toByte() }),
                sections = listOf(
                    headSection,
                    tokenSection,
                    lengthSection,
                    typeSection,
                    contentSection
                ),
            )
        }

        private const val CRC_VALUE = "crc"
        private const val HEAD_VALUE = "head"
        private const val TYPE_VALUE = "type"
        private const val TOKEN_VALUE = "token"
        private const val LENGTH_VALUE = "length"
        private const val CONTENT_VALUE = "content"
    }

    /**
     * Interface for all parsed pulse oximeter data packets
     *
     * This sealed interface represents the various types of data that can be
     * received from a pulse oximeter device, including device information,
     * measurement data, and status updates
     */
    sealed interface Filled : BlePacket.Data {

        /**
         * Contains device product information including version numbers and name
         *
         * @property softwareVersion version of the device firmware
         * @property hardwareVersion version of the device hardware
         * @property productName name of the product
         */
        data class ProductInfo(
            val softwareVersion: String,
            val hardwareVersion: Int,
            val productName: String,
        ) : Filled {

            companion object {

                /**
                 * Extracts product information from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured ProductInfo object
                 */
                fun extractFrom(content: FilledValue<*>): ProductInfo {
                    val innerPacket = content.convertToPacket {
                        section(name = "ProductInfo") {
                            bytes(name = "softwareVersion", size = { 2.bytes })
                            int(name = "hardwareVersion", size = { 1.bytes })
                            string(
                                name = "productName",
                                size = { content.size - 3.bytes },
                            )
                        }
                    }
                    val softwareVersion = innerPacket
                        .findByName<ByteArray>(name = "softwareVersion")
                        .value
                        .toHexString()
                        .split("")
                        .drop(1)
                        .dropLast(1)
                        .map(String::hexToInt)
                        .joinToString(".")

                    return ProductInfo(
                        softwareVersion = softwareVersion,
                        hardwareVersion = innerPacket.findByName<Int>(name = "hardwareVersion").value,
                        productName = innerPacket.findByName<String>(name = "productName").value,
                    )
                }
            }
        }

        /**
         * Contains the device serial number
         *
         * @property serialNumber unique identifier for the device
         */
        data class SerialNumber(val serialNumber: String) : Filled {

            companion object {

                /**
                 * Extracts serial number from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured SerialNumber object
                 */
                fun extractFrom(content: FilledValue<*>): SerialNumber {
                    val serialNumber = content.map {
                        string(name = "serialNumber", size = packetSize)
                    }.value

                    return SerialNumber(serialNumber = serialNumber)
                }
            }
        }

        /**
         * Contains information about the device's battery power level
         *
         * @property level categorized battery level status
         */
        data class BatteryPower(val level: BatteryLevel) : Filled {

            /**
             * Represents different battery power levels
             *
             * @property level byte value representing the battery level
             */
            enum class BatteryLevel(val level: Byte) {

                UNKNOWN(level = -1),
                CRITICAL_LOW(level = 0),
                LOW(level = 1),
                NORMAL(level = 2),
                PLENTIFUL(level = 3),
                ;

                companion object {

                    /**
                     * Converts a raw byte value to the corresponding battery level
                     *
                     * @param level byte value to convert
                     * @return matching BatteryLevel or UNKNOWN if no match
                     */
                    fun fromByte(level: Byte) = entries
                        .firstOrNull { batLevel -> batLevel.level == level }
                        ?: UNKNOWN
                }
            }

            companion object {

                /**
                 * Extracts battery power information from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured BatteryPower object
                 */
                fun extractFrom(content: FilledValue<*>): BatteryPower {
                    val batteryLevel = content.map { byte(name = "batLevel") }.value

                    return BatteryPower(level = BatteryLevel.fromByte(level = batteryLevel))
                }
            }
        }

        /**
         * Contains the frequency at which SpO2 parameters will be sent
         *
         * @property frequency how often parameter updates will be sent
         */
        data class FrequencySpoParameters(val frequency: Int) : Filled {

            companion object {

                /**
                 * Extracts SpO2 parameter frequency from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured FrequencySpoParameters object
                 */
                fun extractFrom(content: FilledValue<*>): FrequencySpoParameters {
                    val frequency = content.map {
                        int(name = "frequency", size = packetSize)
                    }.value

                    return FrequencySpoParameters(frequency = frequency)
                }
            }
        }

        /**
         * Contains the frequency at which SpO2 waveform data will be sent
         *
         * @property frequency how often waveform updates will be sent
         */
        data class FrequencySpoWaveform(val frequency: Int) : Filled {

            companion object {

                /**
                 * Extracts SpO2 waveform frequency from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured FrequencySpoWaveform object
                 */
                fun extractFrom(content: FilledValue<*>): FrequencySpoWaveform {
                    val frequency = content.map {
                        int(name = "frequency", size = packetSize)
                    }.value

                    return FrequencySpoWaveform(frequency = frequency)
                }
            }
        }

        /**
         * Contains plethysmographic waveform data for SpO2 visualization
         *
         * @property waveForms list of waveform points that represent the pulse wave
         */
        data class SpoWaveformData(val waveForms: List<WaveForm>) : Filled {

            /**
             * Represents a single point in the SpO2 waveform
             *
             * @property pulseBeatFlag indicates if this point corresponds to a detected heartbeat
             * @property waveForm amplitude value of the waveform at this point
             */
            data class WaveForm(
                val pulseBeatFlag: Boolean,
                val waveForm: Int,
            )

            companion object {

                /**
                 * Extracts SpO2 waveform data from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured SpoWaveformData object with multiple waveform points
                 */
                fun extractFrom(content: FilledValue<*>): SpoWaveformData {
                    val innerPacket = content.convertToPacket {
                        section(name = "SpoWaveformData") {
                            repeat(5) { index ->
                                custom(name = "wave$index", size = { 1.bytes }) { bytes ->
                                    val byte = ByteBuffer.wrap(bytes)
                                        .order(ByteOrder.LITTLE_ENDIAN)
                                        .get()
                                        .toInt()
                                    WaveForm(
                                        pulseBeatFlag = (byte and 0x80) > 0,
                                        waveForm = byte and 0x7F
                                    )
                                }
                            }
                        }
                    }

                    val waveForms = List(5) { index ->
                        innerPacket.findByName<WaveForm>(name = "wave$index").value
                    }

                    return SpoWaveformData(waveForms = waveForms)
                }
            }
        }

        /**
         * Contains SpO2 and pulse rate measurement parameters
         *
         * @property spoData oxygen saturation value as percentage
         * @property pulse pulse rate in beats per minute
         * @property perfusionIndex perfusion index value (strength of pulse signal)
         * @property status current status of the oximeter sensor and device
         */
        data class SpoParameterPacket(
            val spoData: Int,
            val pulse: Int,
            val perfusionIndex: Float,
            val status: StatusOximeter,
        ) : Filled {

            /**
             * Contains status information about the oximeter device and probe
             *
             * @property isProbeDiconnected whether the probe is physically disconnected
             * @property isProbeOff whether the probe is off the measurement site
             * @property pulseSearchingFlag whether the device is currently searching for a pulse
             * @property isCheckProbe whether the probe needs to be checked
             * @property isMotionDetected whether motion interference is detected
             * @property isLowPerfusion whether the perfusion index is too low for reliable readings
             * @property mode current operating mode of the device
             * @property batteryLevel current battery level
             */
            data class StatusOximeter(
                val isProbeDiconnected: Boolean,
                val isProbeOff: Boolean,
                val pulseSearchingFlag: Boolean,
                val isCheckProbe: Boolean,
                val isMotionDetected: Boolean,
                val isLowPerfusion: Boolean,
                val mode: Mode,
                val batteryLevel: BatteryLevel,
            ) {

                /**
                 * Operating modes for the pulse oximeter
                 */
                enum class Mode {

                    ADULT,
                    NEONATE,
                    VETERINARY,
                    UNKNOWN,
                    ;

                    companion object {

                        /**
                         * Converts a raw integer value to the corresponding mode
                         *
                         * @param mode integer value to convert
                         * @return matching Mode or UNKNOWN if no match
                         */
                        fun fromInt(mode: Int) = when (mode) {
                            0x00 -> ADULT
                            0x01 -> NEONATE
                            0x10 -> VETERINARY
                            else -> UNKNOWN
                        }
                    }
                }
            }

            companion object {

                /**
                 * Extracts SpO2 parameter data from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured SpoParameterPacket object
                 */
                fun extractFrom(content: FilledValue<*>): SpoParameterPacket {
                    val innerPacket = content.convertToPacket {
                        section(name = "SpO2Parameter") {
                            int(name = "spoData", size = { 1.bytes })
                            int(
                                name = "pulseRateData",
                                byteOrder = ByteOrder.LITTLE_ENDIAN,
                                size = { 2.bytes },
                            )
                            int(name = "perfusionIndex", size = { 1.bytes })

                            boolean(name = "probeDisconnected")
                            boolean(name = "probeOff")
                            boolean(name = "pulseSearching")
                            boolean(name = "checkProbe")
                            boolean(name = "motionDetect")
                            boolean(name = "lowPerfusion")
                            custom(name = "mode", size = { 2.bits }) { bytes ->
                                val byte = bytes[0].toInt()
                                ((byte and 0x01) shl 1) or ((byte and 0x02) ushr 1)
                            }

                            byte(name = "batteryLevel", size = { 2.bits })
                            reserve(size = { 6.bits })
                        }
                    }

                    // @formatter:off
                    val spoData = innerPacket.findByName<Int>(name = "spoData").value
                    val pulseRateData = innerPacket.findByName<Int>(name = "pulseRateData").value
                    val perfusionIndex = innerPacket.findByName<Int>(name = "perfusionIndex").value

                    val probeOff = innerPacket.findByName<Boolean>(name = "probeOff").value
                    val checkProbe = innerPacket.findByName<Boolean>(name = "checkProbe").value
                    val motionDetect = innerPacket.findByName<Boolean>(name = "motionDetect").value
                    val lowPerfusion = innerPacket.findByName<Boolean>(name = "lowPerfusion").value
                    val pulseSearching = innerPacket.findByName<Boolean>(name = "pulseSearching").value
                    val probeDisconnected = innerPacket.findByName<Boolean>(name = "probeDisconnected").value

                    val mode = innerPacket.findByName<Int>(name = "mode").value
                    val batteryLevel = innerPacket.findByName<Byte>(name = "batteryLevel").value
                    // @formatter:on

                    return SpoParameterPacket(
                        spoData = spoData,
                        pulse = pulseRateData,
                        perfusionIndex = perfusionIndex / 10f,
                        status = StatusOximeter(
                            isProbeDiconnected = probeDisconnected,
                            isProbeOff = probeOff,
                            pulseSearchingFlag = pulseSearching,
                            isCheckProbe = checkProbe,
                            isMotionDetected = motionDetect,
                            isLowPerfusion = lowPerfusion,
                            mode = StatusOximeter.Mode.fromInt(mode = mode),
                            batteryLevel = BatteryLevel.fromByte(level = batteryLevel),
                        )
                    )
                }
            }
        }

        /**
         * Contains device status information
         *
         * This sealed interface represents different device status states, including
         * spot check measurement phases, continuous measurement, and menu states
         */
        sealed interface StatusData : Filled {

            /**
             * Represents status during spot check measurement mode
             *
             * Spot check measurement involves a sequence of states including preparation,
             * measurement, and result reporting
             */
            sealed interface SpotCheckMeasurement : StatusData {

                /**
                 * Device is in idle state, waiting to start measurement
                 */
                data object Idle : SpotCheckMeasurement

                /**
                 * Device is preparing to take a measurement
                 */
                data object Preparing : SpotCheckMeasurement

                /**
                 * Device is actively measuring
                 *
                 * @property remainingTime seconds remaining until measurement completion
                 */
                data class Measuring(val remainingTime: Int) : SpotCheckMeasurement

                /**
                 * Device is reporting measurement results
                 *
                 * @property spoValue oxygen saturation percentage
                 * @property pulseRate pulse rate in beats per minute
                 */
                data class ReportValue(val spoValue: Int, val pulseRate: Int) : SpotCheckMeasurement

                /**
                 * Device is reporting pulse rate analysis results
                 *
                 * @property analyze the analysis result of the pulse pattern
                 */
                data class PulseRateAnalysis(val analyze: AnalyzeResult) : SpotCheckMeasurement {

                    /**
                     * Possible pulse analysis results
                     */
                    enum class AnalyzeResult {

                        NO_IRREGULARITY_FOUND,
                        LITTLE_FAST_PULSE,
                        FAST_PULSE,
                        SHORT_RUN_OF_FAST_PULSE,
                        LITTLE_SLOW_PULSE,
                        SLOW_PULSE,
                        SHORT_PULSE_INTERVAL,
                        IRREGULAR_PULSE_INTERVAL,
                        FAST_PULSE_WITH_SHORT_PULSE_INTERVAL,
                        SLOW_PULSE_WITH_SHORT_PULSE_INTERVAL,
                        SLOW_PULSE_WITH_IRREGULAR_PULSE_INTERVAL,
                        POOR_SIGNAL,
                        UNKNOWN,
                        ;

                        companion object {

                            /**
                             * Converts a result code to the corresponding analysis result
                             *
                             * @param resultCode integer code from the device
                             * @return matching AnalyzeResult or UNKNOWN if no match
                             */
                            fun fromResultCode(resultCode: Int) = when (resultCode) {
                                0x0 -> NO_IRREGULARITY_FOUND
                                0x1 -> LITTLE_FAST_PULSE
                                0x2 -> FAST_PULSE
                                0x3 -> SHORT_RUN_OF_FAST_PULSE
                                0x4 -> LITTLE_SLOW_PULSE
                                0x5 -> SLOW_PULSE
                                0x6 -> SHORT_PULSE_INTERVAL
                                0x7 -> IRREGULAR_PULSE_INTERVAL
                                0x8 -> FAST_PULSE_WITH_SHORT_PULSE_INTERVAL
                                0x9 -> SLOW_PULSE_WITH_SHORT_PULSE_INTERVAL
                                0x0A -> SLOW_PULSE_WITH_IRREGULAR_PULSE_INTERVAL
                                0xFF -> POOR_SIGNAL
                                else -> UNKNOWN
                            }
                        }
                    }
                }

                /**
                 * Measurement process has completed
                 */
                data object MeasurementFinishes : SpotCheckMeasurement
            }

            /**
             * Device is in continuous measurement mode
             */
            data object ContinuousMeasurement : StatusData

            /**
             * Device is in menu mode
             */
            data object Menu : StatusData

            companion object {

                /**
                 * Extracts status data from raw packet content
                 *
                 * @param content the raw packet content to parse
                 * @return structured StatusData object representing the current device state
                 * @throws IllegalArgumentException if content cannot be parsed into a valid status
                 */
                fun extractFrom(content: FilledValue<*>): StatusData {
                    val innerPacket = content.convertToPacket {
                        section(name = "WorkingStatusData") {
                            int(name = "mode", size = { 1.bytes })
                            int(name = "step", size = { 1.bytes })
                            int(name = "para1", size = { 1.bytes })
                            int(name = "para2", size = { 1.bytes })
                        }
                    }

                    val mode = innerPacket.findByName<Int>(name = "mode").value
                    val step = innerPacket.findByName<Int>(name = "step").value
                    val para1 = innerPacket.findByName<Int>(name = "para1").value
                    val para2 = innerPacket.findByName<Int>(name = "para2").value

                    when (mode) {
                        0x01 -> {
                            when (step) {
                                0x00 -> return Idle
                                0x01 -> return Preparing
                                0x02 -> return Measuring(remainingTime = para1)
                                0x03 -> return ReportValue(
                                    spoValue = para1,
                                    pulseRate = para2,
                                )

                                0x04 -> return PulseRateAnalysis(
                                    analyze = PulseRateAnalysis.AnalyzeResult.fromResultCode(
                                        resultCode = para1
                                    )
                                )

                                0x05 -> return MeasurementFinishes
                            }
                        }

                        0x02 -> return ContinuousMeasurement
                        0x03 -> return Menu
                    }

                    throw IllegalArgumentException("Cannot parse StatusData: unrecognized mode=$mode, step=$step")
                }
            }
        }

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : Filled
    }
}
