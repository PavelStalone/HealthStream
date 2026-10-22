plugins {
    id("android.library")
}

android {
    namespace = "ru.health.stream.core.monitor"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.monitor.timber)
}
