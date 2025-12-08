plugins {
    id("health.stream.android.library")
    id("health.stream.android.hilt")
}

android {
    namespace = "ru.health.stream.core.store.healthconnect"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.health.connect.client)

    implementation(project(":core:store"))
    implementation(project(":core:monitor"))
}
