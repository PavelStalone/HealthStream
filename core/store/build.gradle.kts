plugins {
    id("health.stream.android.library")
    id("health.stream.android.hilt")
}

android {
    namespace = "ru.health.stream.core.store"
}

dependencies {
    api(project(":feature:vitals:source"))
}
