package ru.health.stream.core.communication.ble.lib.device

import ru.health.stream.core.communication.ble.lib.packet.builder.FilledPacketBuilder
import ru.health.stream.core.communication.ble.lib.packet.builder.FilledPacketScope
import ru.health.stream.core.communication.ble.lib.packet.builder.PacketStructureBuilder
import ru.health.stream.core.communication.ble.lib.packet.model.FilledPacket
import ru.health.stream.core.communication.ble.lib.packet.model.PacketStructure

/**
 * Namespace for BLE packet-related components
 */
object BlePacket {

    /**
     * Interface for objects that can be converted to raw binary packets
     *
     * Implementations provide access to their underlying binary representation,
     * allowing them to be serialized for transmission to BLE devices
     */
    interface Convertible {

        /**
         * Underlying binary representation of the packet
         */
        val rawPacket: FilledPacket
    }

    /**
     * Represents a deserialized data packet
     *
     * Contains structured data received from a device or prepared for sending
     */
    interface Data

    data class BytesPacket(
        val bytes: List<Byte>
    ) : Data

    /**
     * Defines the structure and serialization rules for a specific packet type
     *
     * @param T the specific data type this definition creates/handles
     */
    abstract class Definition<T : Data> {

        /**
         * Defines the packet's binary structure and field layout
         */
        abstract val structure: PacketStructure

        /**
         * Converts binary packet data into typed data object
         *
         * @param rawPacket binary packet data
         * @return Typed data object containing the packet's information
         */
        abstract fun parse(rawPacket: FilledPacket): T

        companion object {

            val default = object : Definition<BytesPacket>() {

                override val structure: PacketStructure = PacketStructureBuilder.packetStructure {
                    section(name = "Data") {
                        bytes(name = "data", size = { packetSize() })
                    }
                }

                override fun parse(rawPacket: FilledPacket): BytesPacket = BytesPacket(
                    bytes = rawPacket.findByName<ByteArray>("data").value.toList()
                )
            }
        }
    }

    /**
     * Creates a filled packet using the provided configuration block
     *
     * This function uses a DSL approach to define packet contents in a readable
     * and type-safe manner. The resulting object can be converted to a binary
     * representation for transmission
     *
     * @param block configuration lambda defining the packet's content
     * @return Convertible object containing the constructed packet
     */
    fun filledPacket(
        block: FilledPacketScope.() -> Unit
    ): Convertible = object : Convertible {
        override val rawPacket: FilledPacket = FilledPacketBuilder.filledPacket(block = block)

        override fun toString(): String = rawPacket.toString()
    }
}
