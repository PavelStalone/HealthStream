plugins {
    id("android.library")
    id("android.hilt")
    id("kotlin.room")
    id("kotlin.serialization")
}

android {
    namespace = "ru.health.stream.source.local.room"
}

dependencies {
    implementation(projects.source.local)

    implementation(projects.core.monitor)

    implementation(libs.sqlcipher.android)

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
