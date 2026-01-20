package ru.health.stream.buildlogic.extension

import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.ksp(dependency: Any) {
    add("ksp", dependency)
}

fun DependencyHandlerScope.kspTest(dependency: Any) {
    add("kspTest", dependency)
}

fun DependencyHandlerScope.kspAndroidTest(dependency: Any) {
    add("kspAndroidTest", dependency)
}

fun DependencyHandlerScope.implementation(dependency: Any) {
    add("implementation", dependency)
}

fun DependencyHandlerScope.testImplementation(dependency: Any) {
    add("testImplementation", dependency)
}

fun DependencyHandlerScope.debugImplementation(dependency: Any) {
    add("debugImplementation", dependency)
}

fun DependencyHandlerScope.androidTestImplementation(dependency: Any) {
    add("androidTestImplementation", dependency)
}
