import ru.health.stream.buildlogic.extension.android
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs
import ru.health.stream.buildlogic.extension.projectJavaVersion

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

    compileOptions {
        sourceCompatibility = projectJavaVersion
        targetCompatibility = projectJavaVersion
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
