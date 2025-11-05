package ru.health.stream.core.communication.ble.lib.packet.model

/**
 * Interface for a value structure within a packet section
 *
 * @param T the type of the value
 */
interface ValueStructure<T> {

    /**
     * Reference to the value that will be populated after parsing
     *
     * Note: During packet configuration, this property doesn't contain an actual value
     * and should only be used for structure definition. The real value becomes
     * available only after packet parsing
     */
    val value: T

    /**
     * Identifier of the value
     */
    val name: String

    /**
     * Function that returns the size of the value in bits
     *
     * Note: This is used only during packet structure definition. It defines how many bits should
     * be allocated for this value in the packet structure
     */
    val size: () -> ValueSize

    /**
     * Parser that can convert raw bytes to the value type
     */
    val valueParser: ValueParser<T>
}

internal class ValueStructureImpl<T>(
    override val name: String,
    override val size: () -> ValueSize,
    override val valueParser: ValueParser<T>,
) : ValueStructure<T> {

    private var _value: T? = null

    var bytes: List<Byte> = emptyList()
        private set

    /**
     * Parses the byte array and fills the value
     *
     * @param byteArray raw bytes to parse
     */
    fun fillValue(byteArray: ByteArray) {
        _value = valueParser.parse(byteArray)
        bytes = byteArray.toList()
    }

    /**
     * Returns the parsed value or throws if value has not been filled
     *
     * @return Parsed value
     * @throws IllegalStateException if value has not been filled via [fillValue]
     */
    override val value: T
        get() = requireNotNull(_value)

    override fun toString(): String = "ValueStructure(name='$name')"
}
