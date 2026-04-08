package ru.health.stream.source.remote.ble.lib.packet.builder

import ru.health.stream.source.remote.ble.lib.packet.model.BaseSectionStructure
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructure
import ru.health.stream.source.remote.ble.lib.packet.model.PacketStructureImpl
import ru.health.stream.source.remote.ble.lib.packet.model.SectionStructure
import ru.health.stream.source.remote.ble.lib.packet.model.SectionStructureImpl
import ru.health.stream.source.remote.ble.lib.packet.model.ValueParser
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize
import ru.health.stream.source.remote.ble.lib.packet.model.ValueSize.Companion.bits
import ru.health.stream.source.remote.ble.lib.packet.model.ValueStructure
import ru.health.stream.source.remote.ble.lib.packet.model.ValueStructureImpl
import ru.health.stream.source.remote.ble.lib.packet.validation.PacketValidation
import ru.health.stream.source.remote.ble.lib.packet.validation.ValidationSetting
import ru.health.stream.source.remote.ble.lib.packet.validation.ValidationSettingImpl
import java.nio.ByteOrder
import java.nio.charset.Charset

internal class PacketScopeImpl(
    private val globalScope: GlobalScopeImpl = GlobalScopeImpl(),
) : PacketScope, ValueDefinitionScope by SectionContentScopeImpl(globalScope = globalScope) {

    private val sections: MutableList<BaseSectionStructure> = mutableListOf()
    private val validations: MutableList<ValidationSetting> = mutableListOf()

    override fun section(
        name: String,
        size: (() -> ValueSize)?,
        isEnabled: () -> Boolean,
        block: SectionContentScope.() -> Unit,
    ): SectionStructure {
        val valueRefs = SectionContentScopeImpl(globalScope = globalScope).apply(block).build()

        return SectionStructureImpl(
            name = name,
            size = size.getOrCalculateSizeFunction(valueRefs),
            valueStructures = valueRefs,
            isEnabled = isEnabled,
        ).also { section ->
            sections.add(section)
        }
    }

    override fun validation(
        validation: PacketValidation,
        values: List<ValueStructure<*>>,
        sections: List<SectionStructure>
    ): ValidationSetting {
        val valueSections = sections.flatMap { section -> section.valueStructures }

        return ValidationSettingImpl(
            validation = validation,
            values = (values + valueSections).distinct()
        ).also { validationSettings ->
            validations.add(validationSettings)
        }
    }

    /**
     * Converts a nullable size function to a non-null function
     *
     * If the provided function is null, creates a function that calculates size
     * by summing all value sizes in the section
     *
     * @param valueStructures list of value structures used to calculate size
     * @return Non-null function that returns size
     */
    private fun (() -> ValueSize)?.getOrCalculateSizeFunction(valueStructures: List<ValueStructure<*>>): () -> ValueSize {
        if (this != null) return this
        return { valueStructures.fold(0.bits) { acc, valueRef -> acc + valueRef.size() } }
    }

    /**
     * Builds the final packet structure
     *
     * @return Configured packet structure
     * @throws IllegalArgumentException if packet contains no sections
     */
    internal fun build(): PacketStructure {
        require(sections.isNotEmpty()) { "Packet must contain at least one section" }

        return PacketStructureImpl(
            validations = validations.toList(),
            sectionStructures = sections.toList(),
        ).also { packetStructureImpl ->
            globalScope.setPacketSize { packetStructureImpl.packetSize }
        }
    }
}

internal class SectionContentScopeImpl(
    globalScope: GlobalScopeImpl
) : SectionContentScope, GlobalScope by globalScope {

    private val valueStructures: MutableList<ValueStructure<*>> = mutableListOf()

    override fun reserve(
        name: String?,
        size: () -> ValueSize
    ): ValueStructure<Unit> = custom(
        name = name ?: "Reserved",
        size = size,
        valueParser = {},
    )

    override fun int(
        name: String,
        byteOrder: ByteOrder,
        size: () -> ValueSize
    ): ValueStructure<Int> = custom(
        name = name,
        size = size,
        valueParser = { bytes ->
            if (byteOrder == ByteOrder.LITTLE_ENDIAN) bytes.reverse()
            ValueParser.IntParser.parse(bytes)
        }
    )

    override fun float(
        name: String,
        size: () -> ValueSize
    ): ValueStructure<Float> = custom(
        name = name,
        size = size,
        valueParser = ValueParser.FloatParser
    )

    override fun boolean(
        name: String,
        size: () -> ValueSize
    ): ValueStructure<Boolean> = custom(
        name = name,
        size = size,
        valueParser = ValueParser.BooleanParser
    )

    override fun byte(name: String, size: () -> ValueSize): ValueStructure<Byte> = custom(
        name = name,
        size = size,
        valueParser = { byteArray -> byteArray[0] }
    )

    override fun bytes(name: String, size: () -> ValueSize): ValueStructure<ByteArray> = custom(
        name = name,
        size = size,
        valueParser = { byteArray -> byteArray }
    )

    override fun string(
        name: String,
        size: () -> ValueSize,
        charset: Charset,
    ): ValueStructure<String> = custom(
        name = name,
        size = size,
        valueParser = { byteArray -> byteArray.toString(charset = charset) }
    )

    override fun <T> custom(
        name: String,
        size: () -> ValueSize,
        valueParser: ValueParser<T>,
    ): ValueStructure<T> {
        require(name.isNotBlank()) { "Value name cannot be blank" }

        return ValueStructureImpl(
            name = name,
            size = size,
            valueParser = valueParser,
        ).also { ref ->
            valueStructures.add(ref)
        }
    }

    override fun include(valueStructure: ValueStructure<*>) {
        valueStructures.add(valueStructure)
    }

    override fun include(valueStructures: List<ValueStructure<*>>) {
        this.valueStructures.addAll(valueStructures)
    }

    /**
     * Builds the list of value structures for this section
     *
     * @return Immutable list of value structures
     */
    internal fun build(): List<ValueStructure<*>> = valueStructures.toList()
}

internal class GlobalScopeImpl(
    packetSize: ValueSize? = null
) : GlobalScope {

    private var _packetSize: (() -> ValueSize)? = null

    override val packetSize: () -> ValueSize
        get() = requireNotNull(_packetSize)

    init {
        packetSize?.let { size -> _packetSize = { size } }
    }

    /**
     * Sets or updates the packet size calculation function
     *
     * This method allows setting the packet size after construction,
     * which is necessary when the packet size depends on values
     * defined within the packet itself
     *
     * @param packetSize function that calculates the packet size
     */
    internal fun setPacketSize(packetSize: () -> ValueSize) {
        _packetSize = packetSize
    }
}
