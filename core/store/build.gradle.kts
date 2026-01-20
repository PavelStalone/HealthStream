plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.store"
}

dependencies {
    api(project(":vitals:source"))

    implementation(project(":monitor"))
}
