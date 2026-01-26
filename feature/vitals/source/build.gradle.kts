plugins {
    id("android.library")
}

android {
    namespace = "ru.health.stream.feature.vitals.source"
}

dependencies {
    api(project(":feature:vitals:data"))
}
