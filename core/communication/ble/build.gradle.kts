plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.communication.ble"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.monitor)
    implementation(projects.core.starter)
    implementation(projects.core.communication.ble.lib)

    implementation(projects.feature.vitals.source)

    // TODO: remove this after migrate uuid from lib for BloodPressure device - shoplikpavel 2026-01-27
    implementation(libs.com.github.movisens.smart.gatt)
}
