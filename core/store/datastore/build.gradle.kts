plugins {
    id("android.library")
    id("android.hilt")
    id("kotlin.serialization")
}

android {
    namespace = "ru.health.stream.core.store.datastore"
}

dependencies {
    implementation(projects.data.personal)

    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    
    implementation(libs.kotlinx.datetime)

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
