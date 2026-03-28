plugins {
    id("android.library")
    id("android.hilt")
    id("kotlin.serialization")

    alias(libs.plugins.androidx.room)
}

android {
    namespace = "ru.health.stream.core.store.room"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation(projects.core.store)
    implementation(projects.core.monitor)

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
