import ru.health.stream.buildlogic.extension.libs

plugins {
    id("com.android.library")
    id("kotlin.android")
    id("org.gradle.android.cache-fix")
}

kotlin {
    jvmToolchain(jdkVersion = libs.versions.java.get().toInt())

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.uuid.ExperimentalUuidApi", // For Kotlin Uuid
            "-Xannotation-default-target=param-property", // https://youtrack.jetbrains.com/issue/KT-73255
        )
    }
}
