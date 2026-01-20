import ru.health.stream.buildlogic.extension.application
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("com.android.application")
    id("kotlin.android")
    id("android.compose")
    id("org.gradle.android.cache-fix")
}

application {
    defaultConfig {
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildFeatures {
        buildConfig = true
    }
}
