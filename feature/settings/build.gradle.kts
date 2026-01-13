plugins {
    id("health.stream.android.feature")
    id("health.stream.android.library.compose")
}

android {
    namespace = "ru.health.stream.feature.settings"
}

dependencies {
    implementation(project(":core:ui"))
}
