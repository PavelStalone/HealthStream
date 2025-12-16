package ru.health.stream.automapper.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider class for [AutoMapperProcessor]
 *
 * This class acts as the entry point for the KSP framework to instantiate the processor.
 * It is registered via `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`
 */
internal class AutoMapperProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates a new instance of [AutoMapperProcessor]
     *
     * @param environment KSP environment containing tools like logger and code generator
     * @return New instance of [AutoMapperProcessor]
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AutoMapperProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
}
