package ru.health.stream.source.remote.ble.lib.structure

import java.nio.ByteBuffer
import kotlin.math.ceil
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes

/**
 * Interface for accumulating binary data at the bit level
 *
 * BitAccumulator provides operations to collect bits from various byte sources
 * into a continuous bit sequence, regardless of byte boundaries. This is particularly
 * useful when constructing packets for network protocols, hardware interfaces, or
 * binary formats where data fields don't align to byte boundaries
 *
 * The interface supports adding bits with specific sizes and retrieving the
 * accumulated result as a byte collection
 */
interface BitAccumulator {

    /**
     * Adds bits from the provided bytes with the specified size
     *
     * This method extracts the specified number of bits from the source bytes
     * and adds them to the accumulated bit sequence at the current position
     *
     * @param bytes source bytes containing the bits to add
     * @param size number of bits to take from the source bytes
     */
    fun add(bytes: List<Byte>, size: ValueSize)

    /**
     * Adds all bits from the provided bytes
     *
     * This is a convenience method that adds all bits from the source bytes
     * to the accumulated bit sequence
     *
     * @param bytes source bytes to add completely
     */
    fun addAll(bytes: List<Byte>)

    /**
     * Returns the accumulated bits as a list of bytes
     *
     * @return List of bytes containing all accumulated bits
     */
    fun get(): List<Byte>
}

/**
 * Implementation of BitAccumulator that uses Int (32-bit) as the intermediate format
 * for bit manipulation operations
 *
 * This implementation efficiently handles bit accumulation using Int-based bitwise operations,
 * with recursive processing for bit sequences that cross 32-bit boundaries. This approach is optimal
 * since Kotlin only allows bit shifting operations on Int and Long types
 *
 * The accumulator maintains an internal bit position and byte storage to track where
 * and how bits are stored, allowing precise bit-level accumulation regardless of
 * byte boundaries
 */
class IntBasedBitAccumulator : BitAccumulator {

    /**
     * Current position in bits where the next bit will be written
     */
    private var bitPosition: Int = 0

    /**
     * Storage for accumulated bytes
     */
    private val byteStorage: MutableList<Byte> = mutableListOf()

    override fun add(
        bytes: List<Byte>,
        size: ValueSize,
    ) {
        if (size.bitSize <= 0 || bytes.isEmpty()) return

        val bitIndex = bitPosition % Byte.SIZE_BITS
        val byteIndex = bitPosition / Byte.SIZE_BITS
        val availableBits = Int.SIZE_BITS - bitIndex

        // If we have enough space in the current integer block to fit all new bits,
        // we can add them in one operation; otherwise, we need to split across blocks
        if (availableBits >= size.bitSize) {
            if (byteIndex >= byteStorage.size) byteStorage.add(0x00)

            // Prepare the existing byte for bit manipulation by converting to int
            val currentValue = byteStorage[byteIndex].convertToInt()

            // Align the new bits at the correct position for insertion
            val valueToAdd = alignBitsForInsertion(
                bitCount = size.bitSize,
                bitOffset = bitIndex,
                sourceBytes = bytes,
            )

            // Combine existing and new bits with bitwise OR
            val newValue = currentValue or valueToAdd

            // Convert the int back to bytes and update storage
            val newBytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(newValue).array()
            val requiredBytes = calculateRequiredByteCount(bitCount = bitIndex + size.bitSize)

            byteStorage.removeAt(index = byteIndex)
            byteStorage.addAll(index = byteIndex, elements = newBytes.take(requiredBytes))

            bitPosition += size.bitSize
        } else {
            val bitReader = bytes.asBitReader()

            // Skip leading non-significant bits to align with the requested size
            val skipBitSize = (bytes.size * Byte.SIZE_BITS).bits - size
            bitReader.next(skipBitSize)

            // First part: Add as many bits as possible to the current int block
            val firstPartSize = availableBits.bits
            add(
                size = firstPartSize,
                bytes = bitReader.next(firstPartSize).toList(),
            )

            // Second part: Add remaining bits to the next block (recursive call)
            val remainingSize = size - firstPartSize
            add(
                size = remainingSize,
                bytes = bitReader.next(remainingSize).toList(),
            )
        }
    }

    override fun addAll(bytes: List<Byte>) {
        add(bytes = bytes, size = bytes.size.bytes)
    }

    override fun get(): List<Byte> = byteStorage.toList()

    /**
     * Converts a single byte to an int for bit manipulation
     *
     * @param this byte to convert
     * @return Int representation of the byte for bit operations
     */
    private fun Byte.convertToInt(): Int {
        return ByteBuffer.wrap(byteArrayOf(this) + ByteArray(Int.SIZE_BYTES - 1)).int
    }

    /**
     * Prepares source bits for insertion at the specified bit offset
     *
     * @param sourceBytes bytes containing bits to insert
     * @param bitOffset bit position where the bits should be inserted
     * @param bitCount number of bits to take from the source
     * @return Int with bits positioned correctly for a bitwise OR operation
     */
    private fun alignBitsForInsertion(
        sourceBytes: List<Byte>,
        bitOffset: Int,
        bitCount: Int,
    ): Int {
        val paddedBytes = ByteArray(Int.SIZE_BYTES - sourceBytes.size) + sourceBytes.toByteArray()
        val sourceInt = ByteBuffer.wrap(paddedBytes).int

        return sourceInt shl (Int.SIZE_BITS - bitCount) ushr bitOffset
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
