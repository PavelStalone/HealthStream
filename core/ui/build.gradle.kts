plugins {
    id("android.library")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.core.ui"
}

dependencies {
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
}
