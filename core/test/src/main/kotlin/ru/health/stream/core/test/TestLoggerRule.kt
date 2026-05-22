package ru.health.stream.core.test

import org.junit.rules.TestWatcher
import org.junit.runner.Description

class TestLoggerRule : TestWatcher() {
    override fun starting(description: Description) {
        println("\n>>> STARTING: ${description.className}#${description.methodName}")
    }

    override fun succeeded(description: Description) {
        println(">>> PASSED: ${description.methodName}")
    }

    override fun failed(e: Throwable?, description: Description) {
        println(">>> FAILED: ${description.methodName}")
        println(">>> ERROR: ${e?.message}")
    }
}
