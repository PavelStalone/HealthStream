plugins {
    id("health.stream.android.library")
    id("health.stream.android.hilt")

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

    implementation(project(":core:store"))
    implementation(project(":core:monitor"))
}
