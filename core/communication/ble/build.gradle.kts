plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.communication.ble"
}

dependencies {
    implementation(project(":core:monitor"))
    implementation(project(":core:starter"))
    implementation(project(":core:communication:ble:lib"))

    implementation(project(":feature:vitals:source"))

    // TODO: remove this after migrate uuid from lib for BloodPressure device - shoplikpavel 2026-01-27
    implementation(libs.com.github.movisens.smart.gatt)
}
