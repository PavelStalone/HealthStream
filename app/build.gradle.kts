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

    implementation(projects.core.ui)
    implementation(projects.core.starter)
    implementation(projects.core.monitor)
    implementation(projects.core.navigation)
    implementation(projects.core.store.datastore)

    implementation(projects.source.remote.ble)
    implementation(projects.source.local.room)
    implementation(projects.source.local.healthconnect)

    implementation(projects.feature.chart)
    implementation(projects.feature.settings)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.user.impl)
    implementation(projects.feature.measurement.impl)
}
