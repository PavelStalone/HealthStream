import ru.health.stream.buildlogic.extension.android
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())

    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property") // https://youtrack.jetbrains.com/issue/KT-73255
    }
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
