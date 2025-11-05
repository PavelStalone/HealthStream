package ru.health.stream.core.communication.ble.lib.packet.validation

import ru.health.stream.core.communication.ble.lib.packet.model.ValueStructure

/**
 * Configuration interface for packet validation
 *
 * ValidationSetting defines which values from a packet structure should be included
 * in validation and what validation algorithm should be applied to them. This creates
 * a reusable validation configuration that can be applied to multiple packets of the
 * same type
 *
 * This interface allows for separation between the validation algorithm itself and
 * the selection of fields to validate, making it possible to use different validation
 * strategies with the same field selection or vice versa
 */
interface ValidationSetting {

    /**
     * The validation algorithm to apply to the selected packet values
     */
    val validation: PacketValidation

    /**
     * List of value structures to include in validation
     */
    val values: List<ValueStructure<*>>
}

class ValidationSettingImpl(
    override val validation: PacketValidation,
    override val values: List<ValueStructure<*>>,
): ValidationSetting
