package ru.health.stream.source.remote.ble.lib.packet.model

import ru.health.stream.source.remote.ble.lib.packet.validation.ValidationSetting
import ru.health.stream.source.remote.ble.lib.structure.BitReader

/**
 * Interface representing the overall structure of a packet
 *
 * Packet is composed of multiple sections, each containing values
 *
 * WARNING: This interface should only be used for packet structure registration.
 * Attempting to access values through this structure before packet parsing
 * will result in exceptions. Use the [FilledPacket] object returned from
 * the parser instead of this structure definition for value access
 */
interface PacketStructure {

    /**
     * List of validation settings configured for this packet
     *
     * Each validation setting specifies a validation algorithm and which
     * values should be included in the validation process
     */
    val validations: List<ValidationSetting>

    /**
     * List of all sections that make up this packet structure
     *
     * Note: These section structures are only definitions and don't contain
     * actual values until a packet is parsed
     */
    val sectionStructures: List<SectionStructure>
}

/**
 * Implementation of the packet structure
 *
 * Provides functionality to validate the entire packet across all sections
 *
 * @property validations list of validation settings configured for this packet
 * @property sectionStructures list of all sections in this packet
 */
internal class PacketStructureImpl(
    override val validations: List<ValidationSetting>,
    override val sectionStructures: List<BaseSectionStructure>,
) : PacketStructure {

    private var _packetSize: ValueSize? = null

    val packetSize: ValueSize
        get() = requireNotNull(_packetSize)

    /**
     * Fills a packet structure with data from the bit reader
     *
     * Reads data for each section in the packet structure and populates them with
     * content from the bit reader. Throws an exception if there is insufficient data
     * or if any error occurs during filling
     *
     * @param bitReader source of bits to read from
     * @return Filled packet with populated data
     * @throws IllegalArgumentException if the packet structure implementation is not supported
     * @throws Exception if there is not enough data or other errors occur
     */
    fun fill(bitReader: BitReader): FilledPacket {
        _packetSize = bitReader.remainingSize
        sectionStructures.forEach { section ->
            section.fillSection(bitReader.next(section.size()))
        }

        return toFilledPacket()
    }

    override fun toString(): String = "PacketStructure(sections=$sectionStructures)"
}
