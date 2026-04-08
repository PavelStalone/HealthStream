package ru.health.stream.source.remote.ble.lib.packet.model

import kotlin.math.max

/**
 * Represents a value size in bits
 *
 * @property bitSize number of bits in the size
 */
@JvmInline
value class ValueSize(val bitSize: Int) {

    init {
        require(bitSize >= 0) { "Size value cannot be negative" }
    }

    /**
     * Adds two sizes and returns a new [ValueSize] object with the total number of bits
     *
     * @param valueSize size to add to the current one
     * @return New [ValueSize] object containing the sum of bits
     */
    operator fun plus(valueSize: ValueSize) = ValueSize(bitSize + valueSize.bitSize)

    /**
     * Subtracts a size from the current one and returns a new [ValueSize]
     *
     * This operation ensures the result is never negative by using a maximum of 0
     * for negative differences
     *
     * @param valueSize size to subtract from the current one
     * @return New [ValueSize] object containing the difference (minimum value of 0)
     */
    operator fun minus(valueSize: ValueSize) = ValueSize(max(0, bitSize - valueSize.bitSize))

    override fun toString() = "$bitSize bits"

    companion object {

        /**
         * Converts an integer to a size in bytes
         *
         * @return [ValueSize] representing the specified number of bytes in bits
         */
        val Int.bytes: ValueSize
            get() = ValueSize(this * 8)

        /**
         * Converts an integer to a size in bits
         *
         * @return [ValueSize] representing the specified number of bits
         */
        val Int.bits: ValueSize
            get() = ValueSize(this)
    }
}
