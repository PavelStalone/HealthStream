package ru.health.stream.core.communication.ble.lib.packet.builder

import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacket
import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacketImpl
import ru.health.stream.core.communication.ble.lib.packet.model.FilledValue
import ru.health.stream.core.communication.ble.lib.packet.model.FilledValueImpl
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bytes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil
import kotlin.math.max

/**
 * Builder for creating filled packets using a DSL
 */
object FilledPacketBuilder {

    /**
     * Creates a filled packet using the provided DSL block
     *
     * @param block DSL configuration block for adding values to the packet
     * @return Constructed FilledPacket instance with all defined values
     */
    fun filledPacket(block: FilledPacketScope.() -> Unit): FilledPacket = FilledPacketScopeImpl()
        .apply(block)
        .build()
}

/**
 * Scope interface for the packet building DSL
 *
 * Provides methods for adding different types of values to a packet
 * with specified sizes and optional names
 */
interface FilledPacketScope {

    val size: ValueSize

    /**
     * Reserves space in the packet without setting a specific value
     *
     * @param size amount of space to reserve in bits
     * @param name optional identifier for the reserved space
     * @return filled value representing the reserved space
     */
    fun reserve(
        size: ValueSize,
        name: String? = null,
    ): FilledValue<Int>

    /**
     * Adds an integer value to the packet
     *
     * @param value integer value to add
     * @param size size of the value in bits (defaults to 4 bytes)
     * @param name optional identifier for the value
     * @return filled value containing the integer
     */
    fun int(
        value: Int,
        size: ValueSize = Int.SIZE_BYTES.bytes,
        order: ByteOrder = ByteOrder.LITTLE_ENDIAN,
        name: String? = null,
    ): FilledValue<Int>

    /**
     * Adds a floating point value to the packet
     *
     * @param value float value to add
     * @param size size of the value in bits (defaults to 4 bytes)
     * @param name optional identifier for the value
     * @return filled value containing the float
     */
    fun float(
        value: Float,
        size: ValueSize = Float.SIZE_BYTES.bytes,
        name: String? = null,
    ): FilledValue<Float>

    /**
     * Adds a boolean value to the packet
     *
     * value = true, size = 2.bits -> 01
     *
     * @param value boolean value to add
     * @param size size of the value in bits (defaults to 1 bit)
     * @param name optional identifier for the value
     * @return filled value containing the boolean
     */
    fun boolean(
        value: Boolean,
        size: ValueSize = 1.bits,
        name: String? = null,
    ): FilledValue<Boolean>

    /**
     * Adds a byte value to the packet
     *
     * @param value byte value to add
     * @param size size of the value (defaults to 1 byte)
     * @param name optional identifier for the value
     * @return filled value containing the byte
     */
    fun byte(
        value: Byte,
        size: ValueSize = 1.bytes,
        name: String? = null,
    ): FilledValue<Byte>

    /**
     * Adds a byte array to the packet
     *
     * @param value byte array to add to the packet
     * @param size size of the value in bits (defaults to the byte array's size)
     * @param name optional identifier for the value
     * @return filled value containing the byte array
     */
    fun bytes(
        value: ByteArray,
        size: ValueSize = value.size.bytes,
        name: String? = null,
    ): FilledValue<ByteArray>

    /**
     * Adds a custom value type to the packet
     *
     * @param value the value to add
     * @param size size of the value in bits
     * @param name optional identifier for the value
     * @param parser function to convert the value to a list of bytes
     * @param T type of the value being added
     * @return filled value containing the custom type
     */
    fun <T> custom(
        value: T,
        size: ValueSize,
        name: String? = null,
        parser: (value: T) -> List<Byte>,
    ): FilledValue<T>
}

/**
 * Implementation of [FilledPacketScope] for building packets
 *
 * Accumulates values and converts them to the appropriate binary representation
 * based on their type and specified size
 */
internal class FilledPacketScopeImpl : FilledPacketScope {

    private val filledValues: MutableList<FilledValue<*>> = mutableListOf()

    override val size: ValueSize
        get() = filledValues.fold(0.bits) { acc, value -> acc + value.size }

    override fun int(value: Int, size: ValueSize, order: ByteOrder, name: String?) = custom(
        value = value,
        size = size,
        name = name,
        parser = { storeValue ->
            ByteBuffer.allocate(requiredBytes(defaultBytes = Int.SIZE_BYTES, valueSize = size))
                .order(order)
                .putInt(storeValue)
                .array()
                .toList()
                .reversed()
        }
    )

    override fun float(value: Float, size: ValueSize, name: String?) = custom(
        value = value,
        size = size,
        name = name,
        parser = { storeValue ->
            ByteBuffer.allocate(requiredBytes(defaultBytes = Float.SIZE_BYTES, valueSize = size))
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(storeValue)
                .array()
                .toList()
                .reversed()
        }
    )

    override fun reserve(size: ValueSize, name: String?) = custom(
        value = 0,
        size = size,
        name = name,
        parser = { List(ceil(size.bitSize / Byte.SIZE_BITS.toFloat()).toInt()) { 0x0 } }
    )

    override fun boolean(value: Boolean, size: ValueSize, name: String?) = custom(
        value = value,
        size = size,
        name = name,
        parser = { storeValue ->
            val bytesCount = ceil(size.bitSize / Byte.SIZE_BITS.toFloat()).toInt()
            List(bytesCount) { index ->
                if (storeValue && bytesCount == index + 1) {
                    (0x01 shl (Byte.SIZE_BITS - (size.bitSize - index * Byte.SIZE_BITS))).toByte()
                } else {
                    (0x00).toByte()
                }
            }
        }
    )

    override fun byte(value: Byte, size: ValueSize, name: String?): FilledValue<Byte> = custom(
        value = value,
        size = size,
        name = name,
        parser = { storeValue -> listOf(storeValue) }
    )

    override fun bytes(value: ByteArray, size: ValueSize, name: String?) = custom(
        value = value,
        size = size,
        name = name,
        parser = { storeValue -> storeValue.toList() }
    )

    override fun <T> custom(
        value: T,
        size: ValueSize,
        name: String?,
        parser: (value: T) -> List<Byte>,
    ): FilledValue<T> {
        val bytes = parser(value)

        return FilledValueImpl(
            name = name ?: "",
            size = size,
            value = value,
            bytes = bytes,
        ).also { filledValue ->
            filledValues.add(filledValue)
        }
    }

    fun build(): FilledPacket = FilledPacketImpl(values = filledValues.toList())

    private fun requiredBytes(defaultBytes: Int, valueSize: ValueSize): Int = max(
        defaultBytes,
        ceil(valueSize.bitSize / Byte.SIZE_BITS.toFloat()).toInt()
    )
}
