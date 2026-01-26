plugins {
    id("android.library")
}

android {
    namespace = "ru.health.stream.core.monitor"
}

dependencies {
    implementation(project(":core:monitor:timber"))
}
