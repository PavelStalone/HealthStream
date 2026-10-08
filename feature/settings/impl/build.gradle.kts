plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.settings.impl"
}

dependencies {
    api(projects.feature.settings.api)

    implementation(projects.core.ui)
}
