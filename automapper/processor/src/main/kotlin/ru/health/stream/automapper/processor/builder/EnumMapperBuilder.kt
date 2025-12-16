package ru.health.stream.automapper.processor.builder

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Strategy for generating mapping code for Enum classes
 *
 * This builder assumes that the source and target Enums have constants with identical names.
 * It maps the enum using `TargetEnum.valueOf(name)`
 */
internal class EnumMapperBuilder : MapperBuilder {

    /**
     * Generates a `valueOf` call for the target Enum using the source Enum's name
     *
     * Example output:
     * ```
     * return TargetEnum.valueOf(name)
     * ```
     */
    override fun buildConversion(from: KSClassDeclaration, to: KSClassDeclaration): CodeBlock =
        buildCodeBlock {
            addStatement("return %T.valueOf(name)", to.toClassName())
        }
}
