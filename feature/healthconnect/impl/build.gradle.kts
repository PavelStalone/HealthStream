plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.healthconnect.impl"
}

dependencies {
    implementation(projects.core.ui)

    implementation(projects.source.local.healthconnect)

    implementation(projects.feature.settings.api)
}
