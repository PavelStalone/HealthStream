package ru.health.stream.source.remote.ble.lib.packet.model

/**
 * Interface representing a value that has been filled from parsed packet data
 *
 * Unlike [ValueStructure], which is used for structure definition,
 * this interface provides access to actual parsed values from packets
 *
 * @param T type of the value
 */
interface FilledValue<T> {

    /**
     * The actual value parsed from packet data
     */
    val value: T

    /**
     * Identifier of the value
     */
    val name: String

    /**
     * Size of the value
     */
    val size: ValueSize

    /**
     * Binary representation of the value
     */
    val bytes: List<Byte>
}

/**
 * Implementation of [FilledValue] that holds parsed values from packets
 *
 * @param T type of the value
 * @property value the actual parsed value
 * @property name identifier of the value
 * @property size size of the value
 * @property bytes binary representation of the value as a byte list
 */
internal data class FilledValueImpl<T>(
    override val value: T,
    override val name: String,
    override val size: ValueSize,
    override val bytes: List<Byte>,
) : FilledValue<T>
