import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*_Factory",
                    "*Module",
                    "*NavKey",
                )
            }
        }
    }
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

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.collection.jvm)
}
