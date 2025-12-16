package ru.health.stream.automapper.annotation

/**
 * Marks a function as a mapping definition for the AutoMapper processor
 *
 * This annotation should be applied to a function inside an interface marked with [AutoMapperModule]
 *
 * @property isReversive if `true` (default), the processor will also generate a reverse mapping function
 * (Target -> Source) in addition to the direct mapping (Source -> Target). If `false`, only the direct mapping is generated
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapper(val isReversive: Boolean = true)
