plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.source.remote.ble"
}

dependencies {
    implementation(projects.source.remote.ble.lib)

    implementation(projects.core.common)
    implementation(projects.core.monitor)
    implementation(projects.core.starter)

    implementation(projects.data.vitals)

    // TODO: remove this after migrate uuid from lib for BloodPressure device - shoplikpavel 2026-01-27
    implementation(libs.com.github.movisens.smart.gatt)
}
