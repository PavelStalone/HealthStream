plugins {
    id("health.stream.android.library")
    id("health.stream.android.hilt")
}

android {
    namespace = "ru.health.stream.core.store.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
}
