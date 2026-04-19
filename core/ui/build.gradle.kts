plugins {
    id("android.library")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.core.ui"
}

dependencies {
    implementation(libs.io.coil.compose)

    implementation(projects.data.vitals)
}
