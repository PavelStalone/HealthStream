plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.user.impl"
}

dependencies {
    api(projects.feature.user.api)

    implementation(projects.data.personal)

    implementation(projects.core.ui)
}
