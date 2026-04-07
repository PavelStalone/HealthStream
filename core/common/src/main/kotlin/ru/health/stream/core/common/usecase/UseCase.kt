package ru.health.stream.core.common.usecase

abstract class UseCase<out R> {

    abstract suspend operator fun invoke(): R
}
