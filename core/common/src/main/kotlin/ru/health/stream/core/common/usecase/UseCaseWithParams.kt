package ru.health.stream.core.common.usecase

abstract class UseCaseWithParams<in P, out R> {

    abstract suspend operator fun invoke(params: P): R
}
