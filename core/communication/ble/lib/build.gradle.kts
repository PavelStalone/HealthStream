plugins {
    id("android.library")
}

android {
    namespace = "ru.health.stream.core.communication.ble.lib"
}

dependencies {
    api(libs.nordic.android.common.uiscanner)

    implementation(libs.kotlinx.datetime)
    implementation(libs.bundles.health.stream.ble)
    implementation(project(":monitor"))
}
