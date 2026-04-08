package ru.health.stream.source.remote.ble.lib.packet.model

/**
 * Interface representing a fully parsed packet with populated data
 *
 * Unlike [PacketStructure], which defines the structure of a packet,
 * this interface provides access to the actual parsed values from a packet
 */
interface FilledPacket {

    /**
     * List of all filled values contained within this packet
     */
    val values: List<FilledValue<*>>

    /**
     * Finds a value by its name, throwing an exception if not found
     *
     * This method provides type-safe access to specific values within the packet
     * based on their defined names in the packet structure
     *
     * @param name identifier of the value to find
     * @return Found value cast to type T
     * @throws NoSuchElementException if no value with the given name exists
     */
    fun <T> findByName(name: String): FilledValue<T>

    /**
     * Finds a value by its name, returning null if not found
     *
     * This method provides type-safe access to specific values within the packet
     * based on their defined names in the packet structure
     *
     * @param name identifier of the value to find
     * @return Found value cast to type T, or null if no value with the given name exists
     */
    fun <T> findByNameOrNull(name: String): FilledValue<T>?
}

/**
 * Implementation of [FilledPacket] that holds all parsed data from a packet
 *
 * @property values list of all filled values contained within this packet
 */
internal data class FilledPacketImpl(
    override val values: List<FilledValue<*>>
) : FilledPacket {

    @Suppress("UNCHECKED_CAST")
    override fun <T> findByName(name: String): FilledValue<T> = values
        .first { value -> value.name == name }
        .let { value -> value as FilledValue<T> }

    @Suppress("UNCHECKED_CAST")
    override fun <T> findByNameOrNull(name: String): FilledValue<T>? = values
        .firstOrNull { value -> value.name == name }
        ?.let { value -> value as FilledValue<T> }
}
