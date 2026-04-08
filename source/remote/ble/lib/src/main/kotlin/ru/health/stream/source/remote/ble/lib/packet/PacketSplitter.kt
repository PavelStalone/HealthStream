package ru.health.stream.source.remote.ble.lib.packet

import no.nordicsemi.android.ble.data.Data
import ru.health.stream.source.remote.ble.lib.packet.model.FilledPacket
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructureImpl
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueStructure
import ru.health.stream.source.remote.ble.lib.packet.model.fillOrNull
import ru.health.stream.source.remote.ble.lib.packet.validation.ValidationSetting
import ru.health.stream.source.remote.ble.lib.structure.asBitReader
import ru.health.stream.core.monitor.logD
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.monitor.logW

/**
 * Splits continuous byte stream into structured packets
 *
 * This class accumulates incoming byte data and attempts to extract valid packets
 * by matching against registered packet structures and validating their contents.
 * It serves as a bridge between raw binary data and structured packet objects
 *
 * @property packetSettings map of packet structures and their corresponding handlers
 */
class PacketSplitter(
    private val packetSettings: Map<PacketStructure, (filledPacket: FilledPacket) -> Unit>,
) {
    // TODO: Add multiple processing modes (buffered accumulation and immediate processing) to enable performance optimization for different use cases - shoplikpavel 2025.07.01

    /**
     * Buffer for storing received bytes until they can be processed into packets
     */
    private val byteBuffer: MutableList<Byte> = mutableListOf()

    /**
     * Adds new data to the buffer and attempts to extract packets
     *
     * This method accumulates incoming bytes and triggers packet processing.
     * If valid packets are found, they are extracted and passed to their consumers
     *
     * @param incomingData data object containing bytes to be processed
     */
    fun pushData(incomingData: Data) {
        logV("pushData called: $incomingData")

        incomingData.value?.let { bytes ->
            byteBuffer.addAll(bytes.toList())
            logV("Buffer updated with ${bytes.size} bytes, new size: ${byteBuffer.size}")

            processBuffer() // TODO: Add initial process of incomingData to speed up the packet parsing process - shoplikpavel 2025.06.30
        }
    }

    /**
     * Attempts to extract packets from the current buffer
     *
     * Iteratively processes the buffer to extract all complete packets.
     * For each offset in the buffer, tries to extract a valid packet.
     * If a packet is found, continues processing the remaining buffer
     * until no more packets can be extracted or the buffer is empty
     */
    private fun processBuffer() {
        var packetsFound: Boolean
        do {
            packetsFound = false

            // TODO: Add minimum size calculation for fixed parameters to avoid unnecessary data conversions and transformations - shoplikpavel 2025.07.01
            for (byteOffset in 0..byteBuffer.size) {
                val bitOffset = byteOffset * Byte.SIZE_BITS

                if (tryExtractPacketAt(bitOffset)) {
                    logD("Successfully identified packet at byte offset $byteOffset")

                    packetsFound = true
                    break
                }
            }
        } while (packetsFound && byteBuffer.isNotEmpty())

        if (!packetsFound) {
            logD("No valid packets found in current buffer of ${byteBuffer.size} bytes")
        }
    }

    /**
     * Attempts to extract a valid packet starting at the specified bit offset
     *
     * For each registered packet structure, this method tries to:
     * 1. Fill the packet with data from the buffer
     * 2. Validate the filled packet
     * 3. Remove the consumed bytes from the buffer
     * 4. Notify the packet consumer
     *
     * @param bitOffset bit position in the buffer where packet extraction should start
     * @return True if a valid packet was extracted, false otherwise
     * @throws IllegalArgumentException if the packet structure is not of the expected implementation type
     */
    private fun tryExtractPacketAt(bitOffset: Int): Boolean {
        for ((packetStructure, consumer) in packetSettings) {
            require(packetStructure is PacketStructureImpl) { "Unexpected PacketStructure implementation: ${packetStructure::class.simpleName}" }

            val bitReader = byteBuffer.asBitReader().apply { next(bitOffset.bits) }

            // Try to fill the packet with data
            val filledPacket = packetStructure.fillOrNull(bitReader) ?: continue

            // Try to validate the packet
            if (!validatePacket(packetStructure, filledPacket)) continue

            // Remove consumed bytes from buffer
            val consumedBytes = bitReader.bitOffset / Byte.SIZE_BITS
            repeat(consumedBytes) { byteBuffer.removeFirstOrNull() }
            logV("Removed $consumedBytes bytes from buffer, new size: ${byteBuffer.size}")

            // Notify consumer about the packet
            consumer(filledPacket)

            return true
        }

        return false
    }

    /**
     * Validates a filled packet against its validation settings
     *
     * Ensures that the packet meets all validation criteria defined in its structure
     *
     * @param packetStructure structure containing validation settings
     * @param filledPacket packet to validate
     * @return True if packet passes all validations, false otherwise
     */
    private fun validatePacket(
        packetStructure: PacketStructureImpl,
        filledPacket: FilledPacket
    ): Boolean {
        return runCatching {
            require(filledPacket.satisfiesAllValidations(packetStructure.validations))
        }.onFailure { error ->
            logW("Validation failed: ${error.javaClass.simpleName}: ${error.message}")
        }.isSuccess
    }

    /**
     * Checks if a filled packet satisfies all validation settings
     *
     * For each validation setting, collects the relevant values from the packet
     * and applies the validation algorithm
     *
     * @param validationSettings list of validation settings to check against
     * @return True if all validations pass, false otherwise
     */
    private fun FilledPacket.satisfiesAllValidations(validationSettings: List<ValidationSetting>): Boolean {
        return validationSettings.all { setting ->
            val targetValueNames = setting.values.map(ValueStructure<*>::name)

            val valuesToValidate = values
                .filter { filledValue -> filledValue.name in targetValueNames }

            setting.validation.validate(valuesToValidate)
        }
    }
}
