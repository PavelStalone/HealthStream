plugins {
    id("android.library")
    id("android.hilt")
    id("kotlin.room")
    id("kotlin.serialization")
}

android {
    namespace = "ru.health.stream.core.store.room"
}

dependencies {
    implementation(projects.feature.personal.source)

    implementation(projects.core.store)
    implementation(projects.core.monitor)

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
