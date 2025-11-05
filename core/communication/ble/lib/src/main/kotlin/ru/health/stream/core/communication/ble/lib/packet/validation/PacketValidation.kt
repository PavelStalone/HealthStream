package ru.health.stream.core.communication.ble.lib.packet.validation

import ru.health.stream.core.communication.ble.lib.packet.model.FilledValue

/**
 * Interface for packet data validation
 *
 * Implementations can provide different validation algorithms
 * to verify packet integrity or correctness
 */
fun interface PacketValidation {

    /**
     * Validates a sequence of filled values from a packet
     *
     * The filled values are provided in the exact same order as they are defined
     * in the packet structure, allowing implementations to process fields in their
     * correct sequence
     *
     * @param filledValues list of filled values from the packet to validate, in structural order
     * @return True if validation passes, false otherwise
     */
    fun validate(filledValues: List<FilledValue<*>>): Boolean
}
