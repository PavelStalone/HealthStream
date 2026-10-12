plugins {
    id("android.library")
    id("android.hilt")
    id("kotlin.room")
    id("kotlin.serialization")
}

android {
    namespace = "ru.health.stream.source.local.room"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ENCRYPT_DATABASE", "false")
        }
        release {
            buildConfigField("boolean", "ENCRYPT_DATABASE", "true")
        }
    }
}

dependencies {
    implementation(projects.source.local)

    implementation(projects.core.monitor)

    implementation(libs.sqlcipher.android)

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
