plugins {
    id("android.application")
    id("android.navigation")
    id("android.hilt")
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
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.health.connect.client)

    implementation(projects.core.ui)
    implementation(projects.core.store)
    implementation(projects.core.store.room)
    implementation(projects.core.store.healthconnect)
    implementation(projects.core.starter)
    implementation(projects.core.monitor)
    implementation(projects.core.navigation)
    implementation(projects.core.communication.ble)

    implementation(projects.feature.chart)
    implementation(projects.feature.vitals)
    implementation(projects.feature.personal)
    implementation(projects.feature.settings)
}
