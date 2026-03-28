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
}
