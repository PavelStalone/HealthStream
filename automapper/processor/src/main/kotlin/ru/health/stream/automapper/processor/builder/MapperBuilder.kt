package ru.health.stream.automapper.processor.builder

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock

/**
 * Interface defining the contract for generating conversion code blocks
 *
 * Implementations of this interface provide specific strategies for mapping different types of classes
 * (e.g., Data classes, Enums, Sealed classes)
 */
internal interface MapperBuilder {

    /**
     * Generates the code block to convert an instance of [from] class to an instance of [to] class
     *
     * @param from source class declaration
     * @param to target class declaration
     * @return [CodeBlock] representing the conversion logic (usually a return statement)
     */
    fun buildConversion(from: KSClassDeclaration, to: KSClassDeclaration): CodeBlock
}
