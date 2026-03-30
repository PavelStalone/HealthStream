import ru.health.stream.buildlogic.extension.application
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("com.android.application")
    id("kotlin.android")
    id("android.compose")
    id("org.gradle.android.cache-fix")
}

application {
    defaultConfig.apply {
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildFeatures.apply {
        buildConfig = true
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
