package ru.health.stream.source.remote.ble.lib.packet.validation.base

import ru.health.stream.source.remote.ble.lib.packet.model.FilledValue
import ru.health.stream.source.remote.ble.lib.packet.validation.PacketValidation
import ru.health.stream.source.remote.ble.lib.structure.IntBasedBitAccumulator

/**
 * CRC-32 implementation of packet validation
 *
 * The algorithm operates on all bytes accumulated from the provided filled values,
 * in the exact order and bit arrangement as specified by the values
 *
 * @property crc the expected CRC-32 value that a valid packet should produce
 */
class CRC32Validation(
    private val crc: () -> Int
) : PacketValidation {

    override fun validate(filledValues: List<FilledValue<*>>): Boolean {
        return calculate(filledValues) == crc()
    }

    companion object {

        private val crcTable
            get() = LongArray(256) { i ->
                var j = i.toLong()

                repeat(8) {
                    j = if ((j and 1L) == 1L) {
                        (j ushr 1) xor 3988292384L
                    } else {
                        j ushr 1
                    }
                }

                j
            }

        /**
         * Calculates the CRC-32 checksum for the given filled values
         *
         * @param filledValues list of filled values whose binary content will be used for CRC calculation
         * @return computed CRC-32 byte value
         */
        fun calculate(filledValues: List<FilledValue<*>>): Int {
            val table = crcTable
            val bitAccumulator = IntBasedBitAccumulator()

            var calculatedCRC: Long = 0

            filledValues.forEach { filledValue ->
                bitAccumulator.add(bytes = filledValue.bytes, size = filledValue.size)
            }

            bitAccumulator.get().forEach { byte ->
                val idx = ((byte.toLong() xor calculatedCRC) and 255L).toInt()
                calculatedCRC = (calculatedCRC ushr 8) xor table[idx]
            }

            return calculatedCRC.toInt()
        }

        /**
         * Calculates the CRC-32 checksum for the given filled values
         *
         * Convenience overload that accepts a variable number of filled values instead of a list
         *
         * @param filledValue vararg of filled values whose binary content will be used for CRC calculation
         * @return computed CRC-32 byte value
         */
        fun calculate(vararg filledValue: FilledValue<*>) = calculate(filledValue.toList())
    }
}
