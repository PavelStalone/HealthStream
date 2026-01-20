plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.monitor.timber"
}

dependencies {
    api(libs.jakewharton.timber)

    implementation(project(":starter"))
}
