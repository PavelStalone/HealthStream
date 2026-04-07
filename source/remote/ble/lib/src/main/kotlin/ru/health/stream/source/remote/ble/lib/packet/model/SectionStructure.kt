package ru.health.stream.source.remote.ble.lib.packet.model

import ru.health.stream.source.remote.ble.lib.structure.BitReader
import ru.health.stream.source.remote.ble.lib.structure.asBitReader

/**
 * Interface for a section structure within a packet
 *
 * Sections are logical groups of values that can be enabled or disabled
 * as a whole unit within a packet structure
 */
interface SectionStructure {

    /**
     * Identifier of the section
     */
    val name: String

    /**
     * Function that returns the size of the section in bits
     *
     * Note: This is used only during packet structure definition. It defines how many bits
     * should be allocated for this section in the packet structure
     */
    val size: () -> ValueSize

    /**
     * Function that determines if this section is enabled in the packet
     *
     * Note: This is evaluated during packet parsing to determine if the section
     * should be processed. Can be used for conditional sections that depend on
     * flags or other values in the packet
     */
    val isEnabled: () -> Boolean

    /**
     * List of value structures contained within this section
     */
    val valueStructures: List<ValueStructure<*>>
}

/**
 * Base implementation for all section structures
 *
 * Provides common functionality for filling section data from byte arrays
 * and validation of the section
 *
 * @property name identifier of the section
 * @property size function that returns the size of the section in bits
 * @property isEnabled function that determines if this section is enabled in the packet
 * @property valueStructures list of value structures contained within this section
 */
internal sealed class BaseSectionStructure(
    override val name: String,
    override val size: () -> ValueSize,
    override val isEnabled: () -> Boolean,
    override val valueStructures: List<ValueStructure<*>>,
) : SectionStructure {

    /**
     * Fills the section's values from a byte array
     *
     * Iterates through all value structures in the section and assigns
     * the appropriate portion of the byte array to each value
     *
     * @param byteArray raw bytes to parse
     */
    fun fillSection(byteArray: ByteArray) {
        val bitReader: BitReader = byteArray.copyOf().asBitReader()

        valueStructures.forEach { valueStructure ->
            with(valueStructure as ValueStructureImpl<*>) {
                fillValue(bitReader.next(size = size()))
            }
        }
    }

    override fun toString(): String =
        "SectionStructure(name='$name', valueStructures=$valueStructures)"
}

/**
 * Standard implementation of a section structure
 *
 * @param name identifier of the section
 * @param size function that returns the size of the section in bits
 * @param isEnabled function that determines if this section is enabled in the packet
 * @param valueStructures list of value structures contained within this section
 */
internal class SectionStructureImpl(
    name: String,
    size: () -> ValueSize,
    isEnabled: () -> Boolean,
    valueStructures: List<ValueStructure<*>>,
) : BaseSectionStructure(
    name = name,
    size = size,
    isEnabled = isEnabled,
    valueStructures = valueStructures,
)
