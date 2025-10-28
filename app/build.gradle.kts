plugins {
    id("health.stream.android.application")
    id("health.stream.android.hilt")
}

android {
    namespace = "ru.health.stream"

    defaultConfig {
        applicationId = "ru.health.stream"

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {

    implementation("androidx.health.connect:connect-client:1.2.0-alpha02")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(project(":core:ui"))
}