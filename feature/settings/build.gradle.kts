plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.settings"
}

dependencies {
    implementation(projects.core.ui)
}
