plugins {
    id("health.stream.android.library")
}

android {
    namespace = "ru.health.stream.feature.vitals.data"
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
