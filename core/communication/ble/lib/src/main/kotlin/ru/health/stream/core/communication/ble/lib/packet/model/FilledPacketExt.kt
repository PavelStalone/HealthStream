package ru.health.stream.core.communication.ble.lib.packet.model

import ru.health.stream.core.communication.ble.lib.structure.BitReader
import ru.health.stream.core.communication.ble.lib.structure.IntBasedBitAccumulator
import ru.health.stream.core.monitor.Logger.logw

/**
 * Attempts to fill a packet structure with data from the bit reader
 *
 * Reads data for each section in the packet structure and fills them with
 * content from the bit reader. Returns null if filling fails due to
 * insufficient data or other errors
 *
 * @param bitReader source of bits to read from
 * @return Filled packet if successful, null otherwise
 */
fun PacketStructure.fillOrNull(
    bitReader: BitReader,
): FilledPacket? = runCatching {
    require(this is PacketStructureImpl) { "Unexpected PacketStructure implementation: ${this::class.simpleName}" }

    fill(bitReader)
}.onFailure { throwable ->
    logw("Failed to fill packet: ${throwable.javaClass.simpleName}: ${throwable.message}")
}.getOrNull()

/**
 * Converts a filled packet to its binary representation
 *
 * Accumulates all value bytes into a continuous bit sequence according to their
 * defined sizes, preserving the exact order of values in the packet
 *
 * @return List of bytes representing the packet's binary data
 */
fun FilledPacket.asBytes(): List<Byte> {
    val bitAccumulator = IntBasedBitAccumulator()
    values.forEach { value ->
        bitAccumulator.add(value.bytes, value.size)
    }

    return bitAccumulator.get()
}

/**
 * Converts a packet structure to a filled packet
 *
 * Creates a filled packet instance containing all sections and values
 * from the structure with their current content
 *
 * @return Filled packet containing all sections and values
 */
internal fun PacketStructure.toFilledPacket(): FilledPacket = FilledPacketImpl(
    values = sectionStructures.flatMap { section -> section.toFilledValues() },
)

/**
 * Converts a section structure to a filled section
 *
 * Creates a filled section instance containing all values
 * from the structure with their current content
 *
 * @return Filled section containing all values
 */
internal fun SectionStructure.toFilledValues(): List<FilledValue<*>> = valueStructures
    .map { value -> value.toFilledValue() }

/**
 * Converts a value structure to a filled value
 *
 * Creates a filled value instance containing the actual value and its
 * binary representation from the structure
 *
 * @return Filled value containing the actual value and its binary representation
 * @throws IllegalArgumentException if the value structure is not of the expected implementation type
 */
internal fun <T> ValueStructure<T>.toFilledValue(): FilledValue<T> {
    require(this is ValueStructureImpl<T>) { "Unexpected ValueStructure implementation: ${this::class.simpleName}" }

    return FilledValueImpl(
        name = name,
        size = size(),
        value = value,
        bytes = bytes,
    )
}
