package ru.health.stream.automapper.processor.builder

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Factory for creating [MapperBuilder] instances based on the source and target types
 *
 * This factory inspects the KSP symbols of the source and target classes to decide which
 * generation strategy is appropriate (Enum, Sealed, or Data class)
 */
internal object MapperBuilderFactory {

    /**
     * Returns an appropriate [MapperBuilder] for the given source and target classes
     *
     * - Returns [EnumMapperBuilder] if both source and target are Enums
     * - Returns [SealedMapperBuilder] if both source and target are Sealed classes/interfaces
     * - Returns [DataMapperBuilder] otherwise (default strategy for Data classes)
     *
     * @param logger logger for reporting information or warnings during builder creation
     * @param source source class declaration
     * @param target target class declaration
     * @return Concrete implementation of [MapperBuilder]
     */
    fun getMapperBuilder(
        logger: KSPLogger,
        source: KSClassDeclaration,
        target: KSClassDeclaration,
    ): MapperBuilder {
        val isSourceEnum = source.classKind == ClassKind.ENUM_CLASS
        val isTargetEnum = target.classKind == ClassKind.ENUM_CLASS

        val isSourceSealed = source.modifiers.contains(Modifier.SEALED)
        val isTargetSealed = target.modifiers.contains(Modifier.SEALED)

        return when {
            isSourceEnum && isTargetEnum -> EnumMapperBuilder()
            isSourceSealed && isTargetSealed -> SealedMapperBuilder(logger)
            else -> DataMapperBuilder()
        }
    }
}
