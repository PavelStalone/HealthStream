plugins {
    id("android.application")
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

    implementation(project(":core:ui"))
    implementation(project(":core:store"))
    implementation(project(":core:store:room"))
    implementation(project(":core:store:healthconnect"))
    implementation(project(":core:starter"))
    implementation(project(":core:monitor"))
    implementation(project(":core:communication:ble"))

    implementation(project(":feature:chart"))
    implementation(project(":feature:vitals"))
    implementation(project(":feature:settings"))
}
