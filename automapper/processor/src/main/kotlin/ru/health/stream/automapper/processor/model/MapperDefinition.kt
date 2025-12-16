package ru.health.stream.automapper.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Represents a defined mapping between two classes
 *
 * This data class holds the metadata required to generate a mapper function,
 * extracted from the `@AutoMapper` annotation and the function signature
 *
 * @property source source class declaration (input type)
 * @property target target class declaration (output/return type)
 * @property isReversive Flag indicating whether a reverse mapping (Target -> Source) should also be generated
 */
internal data class MapperDefinition(
    val source: KSClassDeclaration,
    val target: KSClassDeclaration,
    val isReversive: Boolean,
)
