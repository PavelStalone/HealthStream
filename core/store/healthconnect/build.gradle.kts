plugins {
    id("android.library")
    id("android.compose")
    id("android.hilt")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.core.store.healthconnect"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.health.connect.client)

    implementation(projects.core.ui)
    implementation(projects.core.monitor)
    implementation(projects.core.starter)
    implementation(projects.core.navigation)

    implementation(projects.feature.settings)
}
