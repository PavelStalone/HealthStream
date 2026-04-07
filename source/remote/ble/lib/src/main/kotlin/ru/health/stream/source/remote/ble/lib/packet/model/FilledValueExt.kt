package ru.health.stream.source.remote.ble.lib.packet.model

import ru.health.stream.source.remote.ble.lib.packet.builder.GlobalScopeImpl
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketScope
import ru.health.stream.source.remote.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.source.remote.ble.lib.packet.builder.SectionContentScope
import ru.health.stream.source.remote.ble.lib.packet.builder.SectionContentScopeImpl
import ru.health.stream.source.remote.ble.lib.structure.asBitReader

/**
 * Converts this filled value to another value type using the provided structure definition
 *
 * This function takes the binary data from the current filled value and interprets it
 * according to a new structure definition, creating a new filled value of a different type.
 * It effectively re-interprets the same binary data in a new context
 *
 * @param T source value type
 * @param R target value type
 * @param valueStructure lambda defining the structure of the target value
 * @return new filled value of type R containing the same binary data
 */
fun <T, R> FilledValue<T>.map(valueStructure: SectionContentScope.() -> ValueStructure<R>): FilledValue<R> {
    val structure = SectionContentScopeImpl(globalScope = GlobalScopeImpl(packetSize = this.size))
        .run(valueStructure)

    with(structure as ValueStructureImpl<R>) {
        fillValue(this@map.bytes.asBitReader().next(structure.size()))
    }

    return structure.toFilledValue()
}

/**
 * Converts this filled value to a packet using the provided packet structure definition
 *
 * This function takes the binary data from the current filled value and interprets it
 * as a packet according to the structure defined in the scope. This allows transforming
 * simple values into more complex packet structures
 *
 * @param T source value type
 * @param scope lambda defining the structure of the target packet
 * @return filled packet containing the interpreted binary data
 */
fun <T> FilledValue<T>.convertToPacket(scope: PacketScope.() -> Unit): FilledPacket {
    val packetStructure = PacketStructureBuilder.packetStructure(block = scope)

    return convertToPacket(packetStructure)
}

/**
 * Converts this filled value to a packet using an existing packet structure
 *
 * This function takes the binary data from the current filled value and interprets it
 * as a packet according to the provided packet structure. This variant allows reusing
 * pre-defined packet structures rather than creating them inline
 *
 * @param T source value type
 * @param packetStructure pre-defined structure to use for packet interpretation
 * @return filled packet containing the interpreted binary data
 */
fun <T> FilledValue<T>.convertToPacket(packetStructure: PacketStructure): FilledPacket {
    return requireNotNull(packetStructure.fillOrNull(bytes.asBitReader()))
}
