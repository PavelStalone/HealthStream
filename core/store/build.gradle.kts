plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.store"
}

dependencies {
    api(project(":feature:vitals:source"))
    implementation(project(":feature:settings"))

    implementation(project(":core:monitor"))
}
