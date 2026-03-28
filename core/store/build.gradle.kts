plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.store"
}

dependencies {
    api(projects.feature.vitals.source)
    implementation(projects.feature.settings)

    implementation(projects.core.common)
    implementation(projects.core.monitor)
}
