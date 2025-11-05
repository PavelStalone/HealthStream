plugins {
    id("health.stream.android.library")
    id("health.stream.android.hilt")
}

android {
    namespace = "ru.health.stream.core.monitor.timber"
}

dependencies {
    api(libs.jakewharton.timber)

    implementation(project(":core:starter"))
}
