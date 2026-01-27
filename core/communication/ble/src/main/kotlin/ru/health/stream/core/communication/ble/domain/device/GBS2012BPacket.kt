package ru.health.stream.core.communication.ble.domain.device

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_BIND_RESULT
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_MEASURE_SETTING
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_RECEIVER_AUTH
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_RECEIVER_INIT
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_REGISTRATION_RESPONSE
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_SETTING_CALLBACK
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.DEVICE_A6_WEIGHT_DATA
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.PUSH_USER_INFO_TO_WEIGHT_FOR_A6
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.BindResult
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.ReceiverAuth
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.ReceiverInit
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.RegistrationResponse
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.SettingCallback
import ru.health.stream.core.communication.ble.domain.device.GBS2012BPacket.Filled.WeightData
import ru.health.stream.core.communication.ble.lib.device.BlePacket
import ru.health.stream.core.communication.ble.lib.device.byteArrayOfInts
import ru.health.stream.core.communication.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacket
import ru.health.stream.core.communication.ble.lib.packet.model.PacketStructure
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.core.communication.ble.lib.packet.model.convertToPacket
import ru.health.stream.core.communication.ble.lib.packet.model.map
import ru.health.stream.core.communication.ble.lib.packet.validation.base.CRC32Validation
import ru.health.stream.core.monitor.logE
import java.nio.ByteOrder

internal object GBS2012BPacket {

    val ackResponse = BlePacket.filledPacket {
        bytes(value = byteArrayOfInts(0x00, 0x01, 0x01))
    }

    val unitOfMeasure = BlePacket.filledPacket {
        bytes(value = byteArrayOfInts(0x10, 0x03, 0x10, 0x04, 0x01))
    }

    val bindDevice = BlePacket.filledPacket {
        bytes(value = byteArrayOfInts(0x10, 0x04, 0x00, 0x03, 0x00, 0x01))
    }

    fun loginReply(verifyCode: ByteArray) = BlePacket.filledPacket {
        bytes(value = byteArrayOfInts(0x10, 0x0B, 0x00, 0x08, 0x01))
        bytes(value = verifyCode)
        bytes(value = byteArrayOfInts(0x01, 0x02))
    }

    val timeCommand
        get() = BlePacket.filledPacket {
            bytes(
                value = byteArrayOfInts(
                    0x10, // frame control
                    0x08, // length
                    0x10, 0x02, // command
                    0x03, // flag
                )
            )
            int(value = Clock.System.now().epochSeconds.toInt()) // seconds since epoch
            byte(value = 0x30.toByte()) // zone offset table, 0x30 is UTC
        }

    val dateTimeAlt
        get() = BlePacket.filledPacket {
            val currentTime = Clock.System.now()
            val defaultTimeZone = TimeZone.currentSystemDefault()
            val offset = defaultTimeZone.offsetAt(currentTime).totalSeconds
            val localDateTime = currentTime.toLocalDateTime(defaultTimeZone)

            val command = int(value = 10, size = 2.bytes)
            val flags = int(value = 0x38, size = 1.bytes)
            val utcTime = int(value = currentTime.epochSeconds.toInt())
            val timeZone = int(value = offset / 60 / 15 + 48, size = 1.bytes)

            val year = int(value = localDateTime.year, order = ByteOrder.BIG_ENDIAN, size = 2.bytes)
            val monthValue = int(value = localDateTime.monthNumber, size = 1.bytes)
            val dayOfMonth = int(value = localDateTime.dayOfMonth, size = 1.bytes)
            val hour = int(value = localDateTime.hour, size = 1.bytes)
            val minute = int(value = localDateTime.minute, size = 1.bytes)
            val second = int(value = localDateTime.second, size = 1.bytes)

            if (size.bitSize > 36) {
                int(
                    value = CRC32Validation.calculate(
                        command,
                        flags,
                        utcTime,
                        timeZone,
                        year,
                        monthValue,
                        dayOfMonth,
                        hour,
                        minute,
                        second
                    )
                )
            }
        }

    val requestMeasurement = BlePacket.filledPacket {
        val command = int(
            value = DEVICE_A6_MEASURE_SETTING.commandValue,
            size = 2.bytes,
        )
        val userNumber = byte(value = 0)
        val flag = boolean(value = true, size = 1.bytes)

        if (size.bitSize > 36) {
            int(value = CRC32Validation.calculate(command, userNumber, flag))
        }
    }

    val weightUserInformation = BlePacket.filledPacket {
        val command = int(
            value = PUSH_USER_INFO_TO_WEIGHT_FOR_A6.commandValue,
            size = 2.bytes,
        )
        val userNumber = byte(value = 1)
        val sex = boolean(value = false, size = 1.bytes)
        val age = byte(value = 20)
        val height = int(value = 170, size = 2.bytes)
        val idAthlete = boolean(value = false, size = 1.bytes)
        val athleteLevel = byte(value = 0)
        val weight = bytes(byteArrayOf(-1, -1))

        if (size.bitSize > 36) {
            int(
                value = CRC32Validation.calculate(
                    command,
                    userNumber,
                    sex,
                    age,
                    height,
                    idAthlete,
                    athleteLevel,
                    weight
                )
            )
        }
    }

    object Definition : BlePacket.Definition<Filled>() {

        override val structure: PacketStructure
            get() = packet

        override fun parse(rawPacket: FilledPacket): Filled = runCatching {
            val packetCommand = PacketProfile.fromInt(
                command = rawPacket.findByName<Int>(COMMAND_VALUE).value
            )

            val data = rawPacket.findByName<ByteArray>(DATA_VALUE)

            when (packetCommand) {
                DEVICE_A6_REGISTRATION_RESPONSE -> RegistrationResponse
                DEVICE_A6_BIND_RESULT -> BindResult
                DEVICE_A6_RECEIVER_INIT -> ReceiverInit
                DEVICE_A6_RECEIVER_AUTH -> {
                    val verifyCode = data.value

                    data.convertToPacket {
                        section(name = "Info") {
                            reserve(size = { 10.bytes })
                            int(name = "userNumber", size = { 1.bytes })
                            int(name = "battery", size = { 1.bytes })
                        }
                    }.run {
                        ReceiverAuth(
                            battery = findByNameOrNull<Int>("battery")?.value,
                            userNumber = findByNameOrNull<Int>("userNumber")?.value ?: 0,
                            verifyCode = verifyCode.toList(),
                        )
                    }
                }

                DEVICE_A6_SETTING_CALLBACK -> {
                    val settingAck = data.map { int(name = "settingAck", size = { 2.bytes }) }.value

                    SettingCallback(
                        settingAck = SettingCallback.SettingAck.fromCode(settingAck)
                    )
                }

                DEVICE_A6_WEIGHT_DATA -> {
                    data.convertToPacket {
                        section(name = "Info") {
                            reserve(size = { 10.bytes })
                            int(name = "weight", size = { 2.bytes })
                            int(name = "timestamp", size = { 4.bytes })
                        }
                    }.run {
                        val takenAt = Instant.fromEpochMilliseconds(
                            epochMilliseconds = findByName<Int>("timestamp").value * 1000L
                        )

                        WeightData(
                            unit = 0,
                            takenAt = takenAt,
                            weight = findByName<Int>("weight").value.toFloat(),
                        )
                    }
                }

                else -> Filled.Unknown
            }
        }.onFailure { throwable ->
            logE(throwable, "Error occurred while parsing packet")
        }.getOrDefault(Filled.Unknown)

        private val packet = PacketStructureBuilder.packetStructure {
            section(name = "FrameControl") {
                byte(name = FRAME_CONTROL_VALUE)
            }

            val size = int(name = SIZE_VALUE, size = { 1.bytes })
            section(name = "PacketSize") {
                include(size)
            }

            section(name = "PacketContent") {
                int(name = COMMAND_VALUE, size = { 2.bytes })
                bytes(name = DATA_VALUE, size = { size.value.bytes - 2.bytes })
            }
        }

        private const val DATA_VALUE = "data"
        private const val SIZE_VALUE = "packetSize"
        private const val COMMAND_VALUE = "command"
        private const val FRAME_CONTROL_VALUE = "frameControl"

        internal enum class PacketProfile(val commandValue: Int) {

            DEVICE_A6_REGISTRATION_REQUEST(1),
            DEVICE_A6_REGISTRATION_RESPONSE(2),

            // Bind Request
            DEVICE_A6_BIND_NOTICE(3),
            DEVICE_A6_BIND_RESULT(4),

            DEVICE_A6_UNBIND_NOTICE(5),

            // Unbinding response
            DEVICE_A6_UNBIND_RESULT(6),

            // Login Request, incoming
            DEVICE_A6_RECEIVER_AUTH(7),

            // Login Response
            DEVICE_A6_AUTH(8),

            // Initialization request, incoming
            DEVICE_A6_RECEIVER_INIT(9),
            DEVICE_A6_RESPONSE_INIT(10),
            DEVICE_A6_SETTING_CALLBACK(4096),
            DEVICE_A6_MEASURE_SETTING(18433),
            DEVICE_A6_WEIGHT_DATA(18434),
            PUSH_USER_INFO_TO_WEIGHT_FOR_A6(4097),
            PUSH_TIME_TO_WEIGHT_FOR_A6(4098),
            PUSH_TARGET_TO_WEIGHT_FOR_A6(4099),
            PUSH_UNIT_TO_WEIGHT_FOR_A6(4100),
            PUSH_CLEAR_DATA_TO_WEIGHT_FOR_A6(4101),
            PUSH_FORMULA_TO_WEIGHT_FOR_A6(4102),
            RECEIVE_USER_INFO_TO_WEIGHT_FOR_A6(8193),
            RECEIVE_TARGET_TO_WEIGHT_FOR_A6(8195),
            RECEIVE_UNIT_TO_WEIGHT_FOR_A6(8196),

            UNKNOWN(-1)
            ;

            companion object {

                fun fromInt(command: Int): PacketProfile = entries
                    .firstOrNull { entry -> entry.commandValue == command }
                    ?: UNKNOWN
            }
        }
    }

    /**
     * Represents parsed blood pressure data from the device
     */
    sealed interface Filled : BlePacket.Data {

        data object RegistrationResponse : Filled

        data class ReceiverAuth(
            val battery: Int?,
            val userNumber: Int,
            val verifyCode: List<Byte>,
        ) : Filled

        data class SettingCallback(
            val settingAck: SettingAck,
        ) : Filled {

            enum class SettingAck(private val code: Int) {

                PUSH_TIME_TO_WEIGHT(code = 4098),
                PUSH_USER_INFO_TO_WEIGHT(code = 4097),
                PUSH_UNIT_TO_WEIGHT(code = 4100),
                UNKNOWN(code = -1),
                ;

                companion object {

                    fun fromCode(code: Int) = entries
                        .firstOrNull { entry -> entry.code == code }
                        ?: UNKNOWN
                }
            }
        }

        data object BindResult : Filled

        data object ReceiverInit : Filled

        data class WeightData(
            val unit: Int,
            val weight: Float,
            val takenAt: Instant,
        ) : Filled

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : Filled
    }
}
