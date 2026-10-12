package ru.health.stream.source.remote.ble.domain.device

import android.icu.util.Calendar
import kotlinx.datetime.Instant
import ru.health.stream.source.remote.ble.lib.device.BlePacket
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes
import java.nio.ByteOrder

object TimeStampPacket {

    object Definition : BlePacket.Definition<TimeFilled>() {

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
}
