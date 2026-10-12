package ru.health.stream.source.remote.ble.domain.device

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.health.stream.core.monitor.logE
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.BINDING_DEVICE
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.INITIALIZATION_DEVICE
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.LOGIN_DEVICE
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.REGISTRATION_DEVICE
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.SETTING_CALLBACK
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Definition.PacketProfile.SYNCHRONIZE_MEASUREMENT
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.BindingResponse
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.InitializationDeviceRequest
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.LoginDeviceRequest
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.RegistrationResponse
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.SettingCallback
import ru.health.stream.source.remote.ble.domain.device.GBS2012BPacket.Filled.WeightData
import ru.health.stream.source.remote.ble.lib.device.BlePacket
import ru.health.stream.source.remote.ble.lib.device.byteArrayOfInts
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.source.remote.ble.lib.packet.model.convertToPacket

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

    fun loginResponse(verificationCode: ByteArray) = BlePacket.filledPacket {
        bytes(value = byteArrayOfInts(0x10, 0x0B, 0x00, 0x08, 0x01))
        bytes(value = verificationCode)
        bytes(value = byteArrayOfInts(0x00, 0x02))
    }

    val synchronizeRequest
        get() = BlePacket.filledPacket {
            int(value = 0x10, size = 1.bytes) // FrameControl
            int(value = 0x10, size = 1.bytes) // Length
            int(value = 0x4801, size = 2.bytes) // Command
            int(value = 0x00, size = 1.bytes) // UserNumber
            int(value = 0x01, size = 1.bytes) // Status
        }

    val responseInitializationDevice
        get() = BlePacket.filledPacket {
            val utc = Clock.System.now().epochSeconds

            int(value = 0x10, size = 1.bytes) // FrameControl
            int(value = 0x10, size = 1.bytes) // Length
            int(value = 0x0000A, size = 2.bytes) // Command
            int(value = 0x18, size = 1.bytes) // Flag
            int(value = utc.toInt(), size = 4.bytes) // UTC
            int(value = 0x30, size = 1.bytes) // TimeZone UTC+0
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
                REGISTRATION_DEVICE -> RegistrationResponse
                INITIALIZATION_DEVICE -> InitializationDeviceRequest

                BINDING_DEVICE -> {
                    val result = data.value.first() == 0x01.toByte()

                    BindingResponse(result = result)
                }

                LOGIN_DEVICE -> {
                    data.convertToPacket {
                        section(name = "Info") {
                            bytes(name = "verificationCode", size = { 6.bytes })
                            int(name = "userNumber", size = { 1.bytes })
                            int(name = "battery", size = { 1.bytes })
                        }
                    }.run {
                        LoginDeviceRequest(
                            battery = findByName<Int>("battery").value,
                            userNumber = findByName<Int>("userNumber").value,
                            verificationCode = findByName<ByteArray>("verificationCode").value.toList(),
                        )
                    }
                }

                SETTING_CALLBACK -> {
                    data.convertToPacket {
                        section(name = "body") {
                            int(name = "settingCommand", size = { 1.bytes })
                            boolean(name = "result", size = { 1.bytes })
                        }
                    }.run {
                        SettingCallback(
                            result = findByName<Boolean>("result").value,
                            settingCommand = findByName<Int>("settingCommand").value,
                        )
                    }
                }

                SYNCHRONIZE_MEASUREMENT -> {
                    data.convertToPacket {
                        section(name = "Info") {
                            int(name = "surplusDataCount", size = { 2.bytes })

                            boolean(name = "unit", size = { 2.bits })
                            reserve { 1.bits }
                            boolean(name = "hasUTC", size = { 1.bits })
                            reserve { 10.bits }
                            boolean(name = "hasImpedance", size = { 1.bits })
                            boolean(name = "hasHeartRate", size = { 1.bits })
                            reserve { 2.bytes }

                            int(name = "weight", size = { 2.bytes })
                            int(name = "utc", size = { 4.bytes })
                            int(name = "impedance", size = { 2.bytes })
                            int(name = "heartRate", size = { 1.bytes })
                        }
                    }.run {
                        val unit = findByName<Boolean>(name = "unit").value
                        val weight = findByName<Int>(name = "weight").value
                        val utc = findByName<Int>(name = "utc").value

                        WeightData(
                            unit = unit,
                            takenAt = Instant.fromEpochSeconds(utc.toLong()),
                            weight = weight,
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

            REGISTRATION_DEVICE(2),
            BINDING_DEVICE(4),
            LOGIN_DEVICE(7),
            INITIALIZATION_DEVICE(9),

            SYNCHRONIZE_MEASUREMENT(18434), // 0x4802
            SETTING_CALLBACK(4096), // 0x1000

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
     * Represents parsed weight scale data from the device
     */
    sealed interface Filled : BlePacket.Data {

        data class BindingResponse(
            val result: Boolean,
        ) : Filled

        data class SettingCallback(
            val result: Boolean,
            val settingCommand: Int,
        ) : Filled

        data class LoginDeviceRequest(
            val battery: Int,
            val userNumber: Int,
            val verificationCode: List<Byte>,
        ) : Filled

        data class WeightData(
            val weight: Int,
            val unit: Boolean,
            val takenAt: Instant,
        ) : Filled

        data object RegistrationResponse : Filled
        data object InitializationDeviceRequest : Filled

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : Filled
    }
}
