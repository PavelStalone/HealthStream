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

    implementation(project(":ui"))
    implementation(project(":starter"))
    implementation(project(":monitor"))

    implementation(project(":store"))
    implementation(project(":store:room"))
    implementation(project(":store:healthconnect"))

    implementation(project(":chart"))
    implementation(project(":vitals"))
    implementation(project(":settings"))
}
