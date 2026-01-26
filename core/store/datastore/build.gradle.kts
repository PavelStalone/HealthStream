plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.store.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
}
