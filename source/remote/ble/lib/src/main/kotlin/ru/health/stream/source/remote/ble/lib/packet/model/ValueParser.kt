package ru.health.stream.source.remote.ble.lib.packet.model

import java.nio.ByteBuffer
import no.nordicsemi.android.ble.data.Data

/**
 * Functional interface for parsing byte arrays into typed values
 *
 * Implementations of this interface convert raw byte data into specific
 * value types used within packet structures
 *
 * @param T the type of value this parser produces
 */
fun interface ValueParser<T> {

    /**
     * Parses a byte array into a value of type T
     *
     * @param byteArray raw bytes to parse
     * @return parsed value of type T
     */
    fun parse(byteArray: ByteArray): T

    /**
     * Parser implementation for boolean values
     *
     * Converts a single byte to a boolean value where any non-zero value is
     * considered true, and zero is considered false
     */
    object BooleanParser : ValueParser<Boolean> {

        override fun parse(byteArray: ByteArray): Boolean {
            require(byteArray.isNotEmpty()) { "Cannot parse empty byte array to Boolean" }

            return byteArray.any { byte -> byte > 0 }
        }
    }

    /**
     * Parser implementation for integer values
     *
     * Converts up to 4 bytes into an Int value using big-endian byte ordering
     * If fewer than 4 bytes are provided, they are treated as the least significant bytes
     */
    object IntParser : ValueParser<Int> {

        override fun parse(byteArray: ByteArray): Int {
            require(byteArray.size <= Int.SIZE_BYTES) { "Byte array too large for Int conversion: ${byteArray.size} bytes" }

            return ByteBuffer.wrap(ByteArray(Int.SIZE_BYTES - byteArray.size) + byteArray).int
        }
    }

    /**
     * Parser implementation for floating point values
     *
     * Supports both standard IEEE 754 float (4 bytes) and SFLOAT (2 bytes) formats
     * as defined in the Bluetooth GATT specification
     */
    object FloatParser : ValueParser<Float> {

        override fun parse(byteArray: ByteArray): Float {
            val byteCount = byteArray.size
            require(byteCount == Short.SIZE_BYTES || byteCount == Float.SIZE_BYTES) {
                "Required size for float value is 4 bytes (IEEE 754) or 2 bytes (SFLOAT), got $byteCount"
            }

            return if (byteCount == Float.SIZE_BYTES) {
                ByteBuffer.wrap(byteArray).float
            } else {
                Data(byteArray).getFloatValue(Data.FORMAT_SFLOAT, 0) ?: -1f
            }
        }
    }
}
