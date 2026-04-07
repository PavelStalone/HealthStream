package ru.health.stream.source.remote.ble.lib.packet.builder

import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.SectionStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueParser
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bytes
import ru.health.stream.source.remote.ble.lib.packet.model.ValueStructure
import ru.health.stream.source.remote.ble.lib.packet.validation.PacketValidation
import ru.health.stream.source.remote.ble.lib.packet.validation.ValidationSetting
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * DSL marker annotation for packet structure definition
 *
 * Used to limit the scope of implicit receivers in the DSL,
 * preventing accidental access to outer scopes
 */
@DslMarker
annotation class PacketScopeMarker

/**
 * Builder for creating packet structures using a DSL
 */
@PacketScopeMarker
object PacketStructureBuilder {

    /**
     * Creates a packet structure using the provided DSL
     *
     * @param block DSL function for configuring the packet
     * @return Configured packet structure
     */
    fun packetStructure(
        block: PacketScope.() -> Unit,
    ): PacketStructure = PacketScopeImpl()
        .apply(block)
        .build()
}

/**
 * Top-level scope for packet definition
 *
 * Allows defining sections and shared value structures at the packet level
 */
@PacketScopeMarker
interface PacketScope : ValueDefinitionScope {

    // TODO: Implement a marker mechanism for fast packet detection in byte streams - shoplikpavel 2025.07.01

    /**
     * Defines a standard section within the packet
     *
     * @param name identifier for the section
     * @param size function that dynamically calculates section size, null if size should be
     * determined by contained values
     * @param isEnabled function determining if this section is included in the packet
     * @param block builder function for defining values within the section
     * @return Configured section structure
     */
    fun section(
        name: String,
        size: (() -> ValueSize)? = null,
        isEnabled: () -> Boolean = { true },
        block: SectionContentScope.() -> Unit,
    ): SectionStructure

    /**
     * Configures validation settings for the packet
     *
     * Creates a validation configuration that specifies which validation algorithm
     * to use and which values and sections to include in the validation process
     *
     * @param validation the validation algorithm to apply
     * @param values list of individual values to include in validation
     * @param sections list of sections whose values should be included in validation
     * @return Configured validation settings
     */
    fun validation(
        validation: PacketValidation,
        values: List<ValueStructure<*>> = emptyList(),
        sections: List<SectionStructure> = emptyList(),
    ): ValidationSetting
}

/**
 * Provides access to global packet information across all scopes
 *
 * This interface gives definition scopes access to packet-level properties,
 * enabling value and section definitions to reference global packet context.
 * It allows dynamic size calculations and validations that depend on
 * packet-wide information
 */
@PacketScopeMarker
interface GlobalScope {

    /**
     * Function that returns the total size of the packet
     */
    val packetSize: () -> ValueSize
}

/**
 * Scope for defining section content
 */
@PacketScopeMarker
interface SectionContentScope : ValueDefinitionScope {

    /**
     * Defines reserved space within a section
     *
     * Reserved space represents bits/bytes in the packet that aren't
     * parsed but need to be accounted for in size calculations
     *
     * @param name optional identifier for the reserved space
     * @param size function that dynamically calculates the reserved space size
     * @return Configured value structure with Unit type
     */
    fun reserve(
        name: String? = null,
        size: () -> ValueSize,
    ): ValueStructure<Unit>

    /**
     * Includes existing value structure into the current section
     *
     * @param valueStructure value structure to include
     */
    fun include(valueStructure: ValueStructure<*>)

    /**
     * Includes existing value structures into the current section
     *
     * @param valueStructures list of value structures to include
     */
    fun include(valueStructures: List<ValueStructure<*>>)
}

/**
 * Base scope for defining values
 */
@PacketScopeMarker
interface ValueDefinitionScope : GlobalScope {

    /**
     * Defines an integer value
     *
     * @param name identifier for the value
     * @param byteOrder byte order (endianness) for interpreting the integer bytes
     * @param size function that calculates the value size, defaults to 4 bytes
     * @return Int value structure
     */
    fun int(
        name: String,
        byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
        size: () -> ValueSize = { 4.bytes },
    ): ValueStructure<Int>

    /**
     * Defines a floating point value
     *
     * @param name identifier for the value
     * @param size function that calculates the value size, defaults to 2 bytes
     * @return Float value structure
     */
    fun float(name: String, size: () -> ValueSize = { 2.bytes }): ValueStructure<Float>

    /**
     * Defines a boolean value
     *
     * @param name identifier for the value
     * @param size function that calculates the value size, defaults to 1 bit
     * @return Boolean value structure
     */
    fun boolean(name: String, size: () -> ValueSize = { 1.bits }): ValueStructure<Boolean>

    /**
     * Defines a byte value
     *
     * @param name identifier for the value
     * @param size function that calculates the value size, defaults to 1 byte
     * @return Byte value structure
     */
    fun byte(name: String, size: () -> ValueSize = { 1.bytes }): ValueStructure<Byte>

    /**
     * Defines a byte array value
     *
     * @param name identifier for the value
     * @param size function that calculates the total size of the byte array
     * @return ByteArray value structure
     */
    fun bytes(name: String, size: () -> ValueSize): ValueStructure<ByteArray>

    /**
     * Defines a string value
     *
     * @param name identifier for the value
     * @param size function that calculates the total size of the string in bytes
     * @param charset character set to use for string encoding, defaults to US-ASCII
     * @return String value structure
     */
    fun string(
        name: String,
        size: () -> ValueSize,
        charset: Charset = Charsets.US_ASCII,
    ): ValueStructure<String>

    /**
     * Defines a custom value
     *
     * Creates a value structure with a custom parser for any data type
     *
     * @param name identifier for the value
     * @param size function that dynamically calculates the value size
     * @param valueParser parser that converts raw bytes to the value type
     * @return Configured value structure
     */
    fun <T> custom(
        name: String,
        size: () -> ValueSize,
        valueParser: ValueParser<T>,
    ): ValueStructure<T>
}
