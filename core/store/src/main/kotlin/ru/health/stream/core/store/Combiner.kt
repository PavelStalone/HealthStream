package ru.health.stream.core.store

import kotlinx.datetime.Instant

fun <T> List<List<T>>.mergeByTime(instant: (value: T) -> Instant): List<T> {

}
