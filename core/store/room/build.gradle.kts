plugins {
    id("android.library")
    id("android.hilt")

    kotlin("plugin.serialization") version libs.versions.kotlin
}

android {
    namespace = "ru.health.stream.core.store.room"
}

dependencies {
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation(libs.kotlinx.serialization.json)

    implementation(project(":store"))
    implementation(project(":monitor"))

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
