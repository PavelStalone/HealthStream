package ru.health.stream.automapper.annotation

/**
 * Marks an interface as a module for AutoMapper definitions
 *
 * This annotation serves as the entry point for the AutoMapper processor. It should be applied
 * to an interface containing one or more methods annotated with [AutoMapper]
 *
 * The visibility of the generated mapper extensions will match the visibility of this interface.
 * If the interface is `internal`, the generated extensions will be `internal`
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapperModule
