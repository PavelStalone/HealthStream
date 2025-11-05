package ru.health.stream.core.communication.ble.lib.packet.validation.base

import ru.health.stream.core.communication.ble.lib.packet.model.FilledValue
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize
import ru.health.stream.core.communication.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.core.communication.ble.lib.packet.validation.PacketValidation

/**
 * Size-based implementation of packet validation
 *
 * This validator ensures that the total bit size of all fields in a packet
 * exactly matches an expected size. This is useful for protocols where packet
 * length is fixed or must match a specific value
 *
 * The validator sums the sizes of all filled values and compares the result
 * to the expected total size. This can help detect truncated or corrupted packets,
 * or validate that all expected fields are present
 *
 * @property size expected total size in bits that a valid packet should have
 */
class SizeValidation(
    private val size: () -> ValueSize
) : PacketValidation {

    override fun validate(filledValues: List<FilledValue<*>>): Boolean {
        val valuesSize = filledValues.fold(0.bits) { acc, filledValue -> acc + filledValue.size }

        return valuesSize == size()
    }
}
