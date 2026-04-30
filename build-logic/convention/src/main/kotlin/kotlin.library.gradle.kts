import gradle.kotlin.dsl.accessors._18eb934ad9cb2ad93a504577d19a5d43.implementation
import gradle.kotlin.dsl.plugins._d7211432c01ac2fd0e1bc0840c22ba2e.androidx
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(jdkVersion = libs.versions.java.get().toInt())

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi") // For Kotlin Uuid
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.collection.jvm)
}
