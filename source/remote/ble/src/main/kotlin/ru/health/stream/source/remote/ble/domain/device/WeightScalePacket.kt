package ru.health.stream.source.remote.ble.domain.device

import ru.health.stream.core.monitor.logE
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.TimeFilled
import ru.health.stream.source.remote.ble.lib.device.BlePacket
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.source.remote.ble.lib.packet.model.convertToPacket
import ru.health.stream.source.remote.ble.domain.device.TimeStampPacket.Definition as TimeStampDefinition

internal object WeightScalePacket {

    object Definition : BlePacket.Definition<Filled>() {

        override val structure: PacketStructure
            get() = packet

        override fun parse(rawPacket: FilledPacket): Filled = runCatching {
            val measurementUnit = rawPacket.findByName<Boolean>(MEASUREMENT_UNIT_VALUE).value

            val weight = rawPacket.findByName<Int>(WEIGHT_VALUE).value
            val timestamp = rawPacket.findByNameOrNull<ByteArray>(TIMESTAMP_VALUE)
                ?.convertToPacket { TimeStampDefinition.structure }
                ?.let { packet -> TimeStampDefinition.parse(packet) as? TimeFilled.CurrentTime }
            val userId = rawPacket.findByNameOrNull<Int>(USER_ID_VALUE)?.value
            val height = rawPacket.findByNameOrNull<Int>(HEIGHT_VALUE)?.value

            Filled.WeightScaleData(
                weight = weight,
                userId = userId,
                height = height,
                timestamp = timestamp,
                unit = measurementUnit,
            )
        }.onFailure { throwable ->
            logE(throwable, "Error occurred while parsing packet")
        }.getOrDefault(Filled.Unknown)

        private val packet = PacketStructureBuilder.packetStructure {
            val heightFlag = boolean(name = HEIGHT_FLAG_VALUE)
            val userIdFlag = boolean(name = USER_ID_FLAG_VALUE)
            val timeStampFlag = boolean(name = TIMESTAMP_FLAG_VALUE)
            val measurementUnit = boolean(name = MEASUREMENT_UNIT_VALUE)

            section("Flags") {
                reserve { 4.bits }
                include(heightFlag)
                include(userIdFlag)
                include(timeStampFlag)
                include(measurementUnit)
            }

            section("Weight") {
                int(name = WEIGHT_VALUE) { 2.bytes }
            }

            section(name = "Timestamp", isEnabled = { timeStampFlag.value }) {
                bytes(name = TIMESTAMP_VALUE, size = { 7.bytes })
            }

            section(name = "UserId", isEnabled = { userIdFlag.value }) {
                int(name = USER_ID_VALUE, size = { 1.bytes })
            }

            section(name = "Height", isEnabled = { heightFlag.value }) {
                int(name = HEIGHT_VALUE, size = { 1.bytes })
            }
        }

        private const val HEIGHT_FLAG_VALUE = "heightFlag"
        private const val USER_ID_FLAG_VALUE = "userIdFlag"
        private const val TIMESTAMP_FLAG_VALUE = "timeStampFlag"
        private const val MEASUREMENT_UNIT_VALUE = "measurementUnit"

        private const val HEIGHT_VALUE = "height"
        private const val WEIGHT_VALUE = "weight"
        private const val USER_ID_VALUE = "userId"
        private const val TIMESTAMP_VALUE = "timeStamp"
    }

    /**
     * Represents parsed blood pressure data from the device
     */
    sealed interface Filled : BlePacket.Data {

        data class WeightScaleData(
            val weight: Int,
            val userId: Int?,
            val height: Int?,
            val unit: Boolean,
            val timestamp: TimeFilled.CurrentTime?,
        ) : Filled

        /**
         * Represents an unknown or unrecognized packet type
         */
        data object Unknown : Filled
    }
}
