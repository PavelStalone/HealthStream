plugins {
    id("android.feature")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.feature.settings"
}

dependencies {
    implementation(project(":core:ui"))
}
