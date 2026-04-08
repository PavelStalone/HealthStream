plugins {
    id("android.library")
}

android {
    namespace = "ru.health.stream.source.remote.ble.lib"
}

dependencies {
    api(libs.nordic.android.common.uiscanner)

    implementation(libs.kotlinx.datetime)
    implementation(libs.bundles.health.stream.ble)

    implementation(projects.core.monitor)
}
