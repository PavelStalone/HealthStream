package ru.health.stream.automapper.processor.builder

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import ru.health.stream.automapper.processor.helper.ParameterHelper.buildConstructorParameters

/**
 * Strategy for generating mapping code for data classes
 *
 * This builder generates a constructor call for the target class, passing arguments derived from
 * the source class properties
 */
internal class DataMapperBuilder : MapperBuilder {

    /**
     * Generates a constructor call for the target class
     *
     * Example output:
     * ```
     * return TargetClass(
     *     prop1 = prop1,
     *     prop2 = prop2.toLong(),
     * )
     * ```
     */
    override fun buildConversion(from: KSClassDeclaration, to: KSClassDeclaration): CodeBlock =
        buildCodeBlock {
            val params = buildConstructorParameters(sourceClass = from, targetClass = to)

            add("return %T(\n", to.toClassName())
            indent()
            params.forEach { param -> addStatement("$param,") }
            unindent()
            add(")")
        }
}
