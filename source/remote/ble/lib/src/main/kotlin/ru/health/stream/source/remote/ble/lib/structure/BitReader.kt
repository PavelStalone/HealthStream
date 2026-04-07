package ru.health.stream.source.remote.ble.lib.structure

import java.nio.ByteBuffer
import kotlin.math.ceil
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes

/**
 * Interface for working with binary data at the bit level
 *
 * BitReader provides operations to extract specific bits from a binary data source,
 * allowing precise bit-level access regardless of byte boundaries. This is particularly
 * useful when working with network protocols, hardware interfaces, or binary formats
 * where data fields don't align to byte boundaries
 *
 * The interface supports both sequential reading (advancing through the data) and
 * random access to specific bit ranges
 */
interface BitReader {

    /**
     * The current bit position offset within the data source
     *
     * This value indicates the next bit index to be read or processed.
     * It advances as bits are read sequentially using [next], but remains
     * unchanged when accessing bits randomly via [range]
     *
     * The offset is zero-based, where 0 corresponds to the very first bit
     * of the data source
     */
    val bitOffset: Int

    /**
     * The number of bits remaining to be read from the current position
     *
     * This value indicates how many more bits can be accessed from the current
     * bit offset until the end of the data source is reached. It decreases as
     * bits are consumed through sequential reading operations
     *
     * Can be used to check if there's sufficient data available before attempting
     * to read a specific number of bits
     */
    val remainingSize: ValueSize

    /**
     * Returns the next chunk of data of specified size and advances the position
     *
     * This method extracts bits from the current position and then moves
     * the position forward, allowing sequential reading of a binary stream
     *
     * @param size size of data to retrieve
     * @return Byte array containing the requested bits
     */
    fun next(size: ValueSize): ByteArray

    /**
     * Returns a specific range of bits without advancing the position
     *
     * This method provides random access to any range of bits in the data source
     *
     * @param from start bit position (inclusive)
     * @param to end bit position (exclusive)
     * @return Byte array containing the requested bits
     */
    fun range(from: Int, to: Int): ByteArray
}

/**
 * Implementation of BitReader that uses Int (32-bit) as the intermediate format
 * for bit manipulation operations
 *
 * This implementation efficiently handles bit extraction using Int-based bitwise operations,
 * with recursive processing for ranges larger than 32 bits. This approach is optimal
 * since Kotlin only allows bit shifting operations on Int and Long types
 *
 * @property bytes source byte array containing the binary data to read from
 */
class IntBasedBitReader(
    private val bytes: ByteArray
) : BitReader {

    /**
     * Convenience constructor for creating a reader from a single byte
     *
     * @param byte the single byte to read from
     */
    constructor(byte: Byte) : this(byteArrayOf(byte))

    /**
     * Current bit position for sequential reading operations
     */
    private var currentIndex = 0

    override val bitOffset: Int
        get() = currentIndex

    override val remainingSize: ValueSize
        get() = bytes.size.bytes - currentIndex.bits

    /**
     * Returns the next chunk of data of specified size and advances the position
     *
     * Works like a sliding window over the bits
     *
     * Example: If currentIndex=8 and size=4 bits, this method will:
     * 1. Extract bits 8-12
     * 2. Move currentIndex to 12
     * 3. Return the extracted 4 bits as a byte array
     *
     * @param size size of data to retrieve in bits
     * @return Byte array containing exactly the requested bits
     * @throws IllegalArgumentException if the requested size is invalid (e.g., non-positive)
     * @throws IndexOutOfBoundsException if the requested range exceeds available data
     */
    override fun next(size: ValueSize): ByteArray {
        val endIndex = currentIndex + size.bitSize
        val array = range(from = currentIndex, to = endIndex)

        currentIndex = endIndex
        return array
    }

    /**
     * Returns a specific range of bits without advancing the position
     *
     * The implementation uses Int (32-bit) as the intermediate format for bit manipulation
     * since Kotlin only allows bit shifting operations on Int and Long types. This is why
     * ranges larger than 32 bits are processed recursively in chunks
     *
     * @param from start bit position (inclusive)
     * @param to end bit position (exclusive)
     * @return Byte array containing the requested bits
     * @throws IllegalArgumentException if `from` is negative or `to` is less than `from`
     * @throws IndexOutOfBoundsException if `to` exceeds available data size
     */
    override fun range(from: Int, to: Int): ByteArray {
        require(from >= 0) { "Start position cannot be negative" }
        require(to >= from) { "End position must be greater than start" }
        if (to > bytes.size * Byte.SIZE_BITS) throw IndexOutOfBoundsException("End position exceeds available data")

        if (from == to) return byteArrayOf()

        val bitCount = to - from
        val requiredByteCount = calculateRequiredByteCount(bitCount = bitCount)

        if (requiredByteCount > Int.SIZE_BYTES) {
            val splitPoint = to - Int.SIZE_BITS // Process the last 32 bits separately
            val firstPart = range(from = from, to = splitPoint) // Process first part recursively
            val lastPart = extractBitsToBytes(from = splitPoint, to = to) // Process last 32 bits

            return firstPart + lastPart
        }

        return extractBitsToBytes(from = from, to = to)
    }

    /**
     * Extracts a range of bits that fits within an Int and returns as a byte array
     *
     * @param from start bit position (inclusive)
     * @param to end bit position (exclusive)
     * @return Byte array containing the requested bits
     */
    private fun extractBitsToBytes(from: Int, to: Int): ByteArray {
        val bitCount = to - from
        val requiredByteCount = calculateRequiredByteCount(bitCount = bitCount)

        // Calculate which bytes in the array contain our bits
        val startByteIndex = from / Byte.SIZE_BITS // First byte containing our bits
        val endByteIndex = (to - 1) / Byte.SIZE_BITS // Last byte containing our bits
        val relevantBytes = bytes.copyOfRange(
            fromIndex = startByteIndex,
            toIndex = endByteIndex + 1,
        )

        // Extract the exact bits we need from these bytes
        val relativeStart = from % Byte.SIZE_BITS
        val relativeEnd = relativeStart + bitCount
        val shiftedInt = bytesToInt(
            bytes = relevantBytes,
            from = relativeStart,
            to = relativeEnd,
        )

        // Convert the result back to a byte array of the appropriate size
        return intToBytes(value = shiftedInt, byteCount = requiredByteCount)
    }

    /**
     * Extracts specific bits from a byte array and returns them as an Int
     *
     * Visualization of the process:
     * Original bytes: [01110101] [01110101] [01110101] [01110101]
     * If from=3 and to=19:
     * 1. Convert to Int:   [01110101 01110101 01110101 01110101]
     * 2. Clear bits before 'from': [000}10101 01110101 01110101 01110101]
     * 3. Calculate end offset: 13 bits to discard at the end [00010101 01110101 011{00000 00000000]
     * 4. Shift to final position: [00000000 00000000 10101011 10101011]
     *
     * @param bytes source byte array to extract bits from
     * @param from start bit position within this byte array (inclusive)
     * @param to end bit position within this byte array (exclusive)
     * @return Int containing the extracted bits properly aligned
     */
    private fun bytesToInt(
        bytes: ByteArray,
        from: Int,
        to: Int,
    ): Int {
        val byteCount = bytes.size

        // Create an Int from our bytes, padding with zeros if needed
        val intBytes = if (byteCount < Int.SIZE_BYTES) {
            bytes + ByteArray(Int.SIZE_BYTES - byteCount)
        } else {
            bytes
        }
        val intValue = ByteBuffer.wrap(intBytes).int

        // Clear all bits before the 'from' position to ensure clean extraction
        val clearedStart = intValue shl from ushr from

        // Calculate how many bits to discard at the end of the result
        // This handles cases where we don't want all bits up to a byte boundary
        // Example with byte [01101011] and to=6:
        // We need to discard 2 bits at the end: [0110 10]00
        val endBitOffset = (Byte.SIZE_BITS - 1) - ((to - 1) % Byte.SIZE_BITS)

        return if (byteCount <= Int.SIZE_BYTES) {
            // Standard case: right-shift to remove padding bytes and end bits
            // First part removes padding bytes, second part removes end bits
            clearedStart ushr ((Int.SIZE_BYTES - byteCount) * Byte.SIZE_BITS + endBitOffset)
        } else {
            // Special case: handling byte arrays larger than Int capacity
            // This happens when we're extracting bits that span the 4-byte boundary

            // Calculate how many bits we need from the last byte
            val bitsFromLastByte = Byte.SIZE_BITS - endBitOffset

            // Extract bits from the last byte, padding with zeros to make an Int
            val lastBits = ByteBuffer.wrap(ByteArray(Int.SIZE_BYTES - 1) + bytes[byteCount - 1]).int

            // Combine: shift clearedStart left to make room for bits from last byte,
            // then OR with the right-shifted last bits
            (clearedStart shl bitsFromLastByte) or (lastBits ushr endBitOffset)
        }
    }

    /**
     * Converts an Int to a byte array of specified size
     *
     * Example: Converting Int 0x12345678 to 2 bytes will give [0x56, 0x78]
     *
     * @param value source Int value
     * @param byteCount number of bytes to include in result
     * @return Byte array containing the least significant bytes of the Int
     */
    private fun intToBytes(value: Int, byteCount: Int): ByteArray {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(value)

        return buffer.array().copyOfRange(
            fromIndex = Int.SIZE_BYTES - byteCount,
            toIndex = Int.SIZE_BYTES,
        )
    }

    /**
     * Calculates how many bytes are needed to store the given number of bits
     *
     * @param bitCount number of bits
     * @return Number of bytes needed (rounded up)
     */
    private fun calculateRequiredByteCount(bitCount: Int): Int {
        return ceil(bitCount / Byte.SIZE_BITS.toFloat()).toInt()
    }
}
